package com.bittercode.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bittercode.constant.BookCoinPolicy;
import com.bittercode.constant.ResponseCode;
import com.bittercode.constant.db.ConsignmentDBConstants;
import com.bittercode.constant.db.UsersDBConstants;
import com.bittercode.model.CoinTransaction;
import com.bittercode.model.CoinTransactionType;
import com.bittercode.model.StoreException;
import com.bittercode.service.BookCoinService;
import com.bittercode.util.DBUtil;

/**
 * Triển khai BookCoinService sử dụng JDBC thuần và sổ cái idempotent qua UNIQUE (type, ref_id).
 */
public class BookCoinServiceImpl implements BookCoinService {
    private static final Logger LOGGER = Logger.getLogger(BookCoinServiceImpl.class.getName());

    private static final int MYSQL_ERR_DUPLICATE_ENTRY = 1062;

    @Override
    public long getBalance(String username) throws StoreException {
        if (isBlank(username)) {
            return 0L;
        }
        String sql = "SELECT " + ConsignmentDBConstants.COL_USER_BOOK_COINS
                + " FROM " + UsersDBConstants.TABLE_USERS
                + " WHERE " + UsersDBConstants.COLUMN_USERNAME + " = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(ConsignmentDBConstants.COL_USER_BOOK_COINS) : 0L;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading book_coins balance", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<CoinTransaction> getHistory(String username, int limit, int offset) throws StoreException {
        List<CoinTransaction> result = new ArrayList<>();
        if (isBlank(username)) {
            return result;
        }
        int safeLimit = Math.max(1, limit);
        int safeOffset = Math.max(0, offset);
        String sql = "SELECT id, username, amount, type, ref_id, order_id, balance_after, note, created_at"
                + " FROM " + ConsignmentDBConstants.TABLE_COIN_TRANSACTIONS
                + " WHERE username = ?"
                + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setInt(2, safeLimit);
            ps.setInt(3, safeOffset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CoinTransaction tx = new CoinTransaction();
                    tx.setId(rs.getLong("id"));
                    tx.setUsername(rs.getString("username"));
                    tx.setAmount(rs.getInt("amount"));
                    tx.setType(rs.getString("type"));
                    tx.setRefId(rs.getString("ref_id"));
                    tx.setOrderId(rs.getString("order_id"));
                    tx.setBalanceAfter(rs.getLong("balance_after"));
                    tx.setNote(rs.getString("note"));
                    tx.setCreatedAt(rs.getTimestamp("created_at"));
                    result.add(tx);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading coin_transactions history", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    @Override
    public long calculateMaxRedeemable(long balance, long goodsTotal) {
        if (balance <= 0 || goodsTotal <= BookCoinPolicy.MIN_PAYABLE_VND) {
            return 0L;
        }
        long maxByPercent = (goodsTotal / 100L) * BookCoinPolicy.MAX_REDEEM_PERCENT
                + ((goodsTotal % 100L) * BookCoinPolicy.MAX_REDEEM_PERCENT) / 100L;
        long maxAfterMinPayable = goodsTotal - BookCoinPolicy.MIN_PAYABLE_VND;
        long maxAllowed = Math.min(balance, Math.min(maxByPercent, maxAfterMinPayable));
        return Math.max(0L, maxAllowed);
    }

    @Override
    public boolean redeemForOrder(Connection con, String username, String orderId, long coins) throws StoreException {
        if (coins <= 0 || isBlank(username) || isBlank(orderId)) {
            return false;
        }
        String normalizedOrderId = orderId.trim();
        return applyCoinChange(
                con,
                username.trim(),
                -coins,
                CoinTransactionType.ORDER_REDEEM,
                normalizedOrderId,
                normalizedOrderId,
                "Dùng Book Coins cho đơn hàng " + normalizedOrderId
        );
    }

    @Override
    public boolean refundForOrder(Connection con, String orderId) throws StoreException {
        if (con == null) {
            throw new StoreException(ResponseCode.BAD_REQUEST);
        }
        if (isBlank(orderId)) {
            return false;
        }
        String normalizedOrderId = orderId.trim();
        String query = "SELECT username, " + ConsignmentDBConstants.COL_ORDER_COINS_USED
                + " FROM orders WHERE order_id = ?";
        String username;
        long coinsUsed;
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, normalizedOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                username = rs.getString("username");
                coinsUsed = rs.getLong(ConsignmentDBConstants.COL_ORDER_COINS_USED);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading order coins_used for refund", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }

        if (coinsUsed <= 0 || isBlank(username)) {
            return false;
        }

        return applyCoinChange(
                con,
                username.trim(),
                coinsUsed,
                CoinTransactionType.ORDER_REFUND,
                normalizedOrderId,
                normalizedOrderId,
                "Hoàn Book Coins từ đơn hàng " + normalizedOrderId
        );
    }

    @Override
    public boolean rewardConsignor(Connection con, long usedBookId, String orderId) throws StoreException {
        if (con == null) {
            throw new StoreException(ResponseCode.BAD_REQUEST);
        }
        if (usedBookId <= 0 || isBlank(orderId)) {
            return false;
        }
        String query = "SELECT consignor_username, resale_price FROM "
                + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " WHERE id = ?";
        String consignorUsername;
        int resalePrice;
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, usedBookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                consignorUsername = rs.getString("consignor_username");
                resalePrice = rs.getInt("resale_price");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading used_book for consignor reward", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }

        long reward = resalePrice / BookCoinPolicy.CONSIGNMENT_REWARD_DIVISOR;
        if (reward <= 0 || isBlank(consignorUsername)) {
            return false;
        }

        return applyCoinChange(
                con,
                consignorUsername.trim(),
                reward,
                CoinTransactionType.CONSIGN_REWARD,
                String.valueOf(usedBookId),
                orderId.trim(),
                "Thưởng ký gửi sách #" + usedBookId
        );
    }

    @Override
    public int rewardForCompletedOrder(Connection con, String orderId) throws StoreException {
        if (con == null) {
            throw new StoreException(ResponseCode.BAD_REQUEST);
        }
        if (isBlank(orderId)) {
            return 0;
        }
        String normalizedOrderId = orderId.trim();
        String query = "SELECT id FROM " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " WHERE sold_order_id = ? AND status = ? ORDER BY id ASC";
        List<Long> usedBookIds = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, normalizedOrderId);
            ps.setString(2, com.bittercode.model.ConsignmentStatus.SOLD.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usedBookIds.add(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing sold used_books for order reward", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }

        int rewardedCount = 0;
        for (Long usedBookId : usedBookIds) {
            if (rewardConsignor(con, usedBookId, normalizedOrderId)) {
                rewardedCount++;
            }
        }
        return rewardedCount;
    }

    /**
     * Thực thi thay đổi số dư theo thứ tự an toàn cho giao dịch caller:
     * 1) Khóa dòng users (FOR UPDATE) để kiểm tra số dư thực tế và tránh deadlock giữa 2 luồng.
     * 2) Chèn bản ghi vào coin_transactions TRƯỚC để UNIQUE(type, ref_id) chặn trùng lặp
     *    trước khi đụng vào users.book_coins (tránh thay đổi số dư nếu trùng).
     * 3) Cập nhật users.book_coins có điều kiện (book_coins >= debit nếu trừ).
     */
    private boolean applyCoinChange(Connection con, String username, long signedAmount,
                                    CoinTransactionType type, String refId, String orderId, String note)
            throws StoreException {
        if (con == null) {
            throw new StoreException(ResponseCode.BAD_REQUEST);
        }
        if (signedAmount == 0 || signedAmount > Integer.MAX_VALUE || signedAmount < Integer.MIN_VALUE) {
            return false;
        }

        String lockUserSql = "SELECT " + ConsignmentDBConstants.COL_USER_BOOK_COINS
                + " FROM " + UsersDBConstants.TABLE_USERS
                + " WHERE " + UsersDBConstants.COLUMN_USERNAME + " = ? FOR UPDATE";
        String insertLedgerSql = "INSERT INTO " + ConsignmentDBConstants.TABLE_COIN_TRANSACTIONS
                + " (username, amount, type, ref_id, order_id, balance_after, note)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            long currentBalance;
            try (PreparedStatement psLock = con.prepareStatement(lockUserSql)) {
                psLock.setString(1, username);
                try (ResultSet rs = psLock.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    currentBalance = rs.getLong(ConsignmentDBConstants.COL_USER_BOOK_COINS);
                }
            }

            if (signedAmount < 0 && currentBalance < -signedAmount) {
                return false;
            }
            long balanceAfter = currentBalance + signedAmount;
            if (balanceAfter < 0) {
                return false;
            }

            long insertedLedgerId = -1L;
            try (PreparedStatement psLedger = con.prepareStatement(insertLedgerSql, Statement.RETURN_GENERATED_KEYS)) {
                psLedger.setString(1, username);
                psLedger.setInt(2, (int) signedAmount);
                psLedger.setString(3, type.name());
                psLedger.setString(4, refId);
                psLedger.setString(5, orderId);
                psLedger.setLong(6, balanceAfter);
                psLedger.setString(7, note);
                psLedger.executeUpdate();
                try (ResultSet keys = psLedger.getGeneratedKeys()) {
                    if (keys.next()) {
                        insertedLedgerId = keys.getLong(1);
                    }
                }
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    LOGGER.log(Level.FINE, "Duplicate coin transaction ignored: type={0}, refId={1}",
                            new Object[]{type.name(), refId});
                    return false;
                }
                throw e;
            }

            int updatedRows;
            if (signedAmount < 0) {
                long debit = -signedAmount;
                String deductSql = "UPDATE " + UsersDBConstants.TABLE_USERS
                        + " SET " + ConsignmentDBConstants.COL_USER_BOOK_COINS + " = "
                        + ConsignmentDBConstants.COL_USER_BOOK_COINS + " - ?"
                        + " WHERE " + UsersDBConstants.COLUMN_USERNAME + " = ? AND "
                        + ConsignmentDBConstants.COL_USER_BOOK_COINS + " >= ?";
                try (PreparedStatement psUpd = con.prepareStatement(deductSql)) {
                    psUpd.setLong(1, debit);
                    psUpd.setString(2, username);
                    psUpd.setLong(3, debit);
                    updatedRows = psUpd.executeUpdate();
                }
            } else {
                String creditSql = "UPDATE " + UsersDBConstants.TABLE_USERS
                        + " SET " + ConsignmentDBConstants.COL_USER_BOOK_COINS + " = "
                        + ConsignmentDBConstants.COL_USER_BOOK_COINS + " + ?"
                        + " WHERE " + UsersDBConstants.COLUMN_USERNAME + " = ?";
                try (PreparedStatement psUpd = con.prepareStatement(creditSql)) {
                    psUpd.setLong(1, signedAmount);
                    psUpd.setString(2, username);
                    updatedRows = psUpd.executeUpdate();
                }
            }

            if (updatedRows != 1) {
                // Phòng vệ nếu caller gọi trong chế độ autoCommit=true và bị race ở bước cập nhật users
                deleteLedgerRowQuietly(con, insertedLedgerId);
                return false;
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error applying coin transaction", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteLedgerRowQuietly(Connection con, long ledgerId) {
        if (ledgerId <= 0) {
            return;
        }
        String sql = "DELETE FROM " + ConsignmentDBConstants.TABLE_COIN_TRANSACTIONS + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, ledgerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to cleanup uncommitted ledger row id=" + ledgerId, e);
        }
    }

    private boolean isDuplicateKey(SQLException e) {
        if (e.getErrorCode() == MYSQL_ERR_DUPLICATE_ENTRY) {
            return true;
        }
        return e instanceof SQLIntegrityConstraintViolationException
                && "23000".equals(e.getSQLState())
                && e.getMessage() != null
                && e.getMessage().toLowerCase().contains("duplicate");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
