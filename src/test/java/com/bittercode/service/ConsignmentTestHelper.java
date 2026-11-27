package com.bittercode.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bittercode.util.DBUtil;

/**
 * Tiện ích tạo và dọn dữ liệu kiểm thử có tiền tố "T_" trên CSDL dev.
 */
final class ConsignmentTestHelper {

    static final String PREFIX = "T_";

    private final List<String> usernames = new ArrayList<>();
    private final List<String> orderIds = new ArrayList<>();
    private final List<Long> usedBookIds = new ArrayList<>();
    private final List<String> bookBarcodes = new ArrayList<>();

    static String uniqueName(String base) {
        return PREFIX + base + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    void trackUsername(String username) {
        if (username != null) {
            usernames.add(username);
        }
    }

    void trackOrderId(String orderId) {
        if (orderId != null) {
            orderIds.add(orderId);
        }
    }

    void trackUsedBookId(long id) {
        if (id > 0) {
            usedBookIds.add(id);
            bookBarcodes.add("UB-" + id);
        }
    }

    void trackBarcode(String barcode) {
        if (barcode != null) {
            bookBarcodes.add(barcode);
        }
    }

    String createTestUser(String suffix, long initialCoins) throws Exception {
        String username = uniqueName(suffix);
        trackUsername(username);
        String sql = "INSERT INTO users (username, password, firstname, lastname, address, phone, mailid, usertype, book_coins)"
                + " VALUES (?, 'test_pass', 'Test', 'User', 'Test Addr', '0900000000', ?, 2, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username + "@test.local");
            ps.setLong(3, initialCoins);
            ps.executeUpdate();
        }
        return username;
    }

    String createTestOrder(String username, double totalAmount, int coinsUsed, String status) throws Exception {
        String orderId = uniqueName("ORD");
        trackOrderId(orderId);
        String sql = "INSERT INTO orders (order_id, username, total_amount, status, coins_used)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, username);
            ps.setDouble(3, totalAmount);
            ps.setString(4, status);
            ps.setInt(5, coinsUsed);
            ps.executeUpdate();
        }
        return orderId;
    }

    void addOrderDetail(String orderId, String barcode, int quantity, double amount) throws Exception {
        trackBarcode(barcode);
        String sql = "INSERT INTO order_details (order_id, book_barcode, quantity, amount) VALUES (?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, barcode);
            ps.setInt(3, quantity);
            ps.setDouble(4, amount);
            ps.executeUpdate();
        }
    }

    long insertDirectUsedBook(String consignorUsername, int originalPrice, int resalePrice,
                              int conditionPercent, String status, String bookBarcode, String soldOrderId)
            throws Exception {
        String sql = "INSERT INTO used_books (consignor_username, title, author, original_price, resale_price,"
                + " condition_percent, status, book_barcode, sold_order_id)"
                + " VALUES (?, 'T_Book', 'T_Author', ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, consignorUsername);
            ps.setInt(2, originalPrice);
            ps.setInt(3, resalePrice);
            ps.setInt(4, conditionPercent);
            ps.setString(5, status);
            ps.setString(6, bookBarcode);
            ps.setString(7, soldOrderId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    trackUsedBookId(id);
                    return id;
                }
            }
        }
        throw new IllegalStateException("Could not insert test used_books row");
    }

    int countLedgerRows(String type, String refId) throws Exception {
        String sql = "SELECT COUNT(*) FROM coin_transactions WHERE type = ? AND ref_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, refId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    void cleanup() {
        try (Connection con = DBUtil.getConnection()) {
            for (Long id : usedBookIds) {
                execDelete(con, "DELETE FROM coin_transactions WHERE ref_id = ?", String.valueOf(id));
                execDelete(con, "DELETE FROM admin_notifications WHERE ref_id = ?", String.valueOf(id));
            }
            for (String orderId : orderIds) {
                execDelete(con, "DELETE FROM coin_transactions WHERE ref_id = ? OR order_id = ?", orderId, orderId);
                execDelete(con, "DELETE FROM order_details WHERE order_id = ?", orderId);
                execDelete(con, "DELETE FROM orders WHERE order_id = ?", orderId);
            }
            for (String username : usernames) {
                execDelete(con, "DELETE FROM coin_transactions WHERE username = ?", username);
                execDelete(con,
                        "DELETE FROM used_book_images WHERE used_book_id IN (SELECT id FROM used_books WHERE consignor_username = ?)",
                        username);
                execDelete(con, "DELETE FROM used_books WHERE consignor_username = ?", username);
            }
            for (Long id : usedBookIds) {
                execDelete(con, "DELETE FROM used_book_images WHERE used_book_id = ?", id);
                execDelete(con, "DELETE FROM used_books WHERE id = ?", id);
            }
            for (String barcode : bookBarcodes) {
                execDelete(con, "DELETE FROM books WHERE barcode = ?", barcode);
            }
            for (String username : usernames) {
                execDelete(con, "DELETE FROM users WHERE username = ?", username);
            }
        } catch (Exception ignored) {
            // Đảm bảo @AfterEach không che khuất lỗi assertion của test
        }
    }

    private void execDelete(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        }
    }
}
