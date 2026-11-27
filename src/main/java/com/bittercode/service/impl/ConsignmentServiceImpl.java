package com.bittercode.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bittercode.constant.BookCoinPolicy;
import com.bittercode.constant.ResponseCode;
import com.bittercode.constant.db.BooksDBConstants;
import com.bittercode.constant.db.ConsignmentDBConstants;
import com.bittercode.model.ConsignmentStatus;
import com.bittercode.model.StoreException;
import com.bittercode.model.UsedBook;
import com.bittercode.service.ConsignmentService;
import com.bittercode.util.DBUtil;

/**
 * Triển khai ConsignmentService bằng JDBC thuần và cập nhật có điều kiện theo trạng thái.
 */
public class ConsignmentServiceImpl implements ConsignmentService {
    private static final Logger LOGGER = Logger.getLogger(ConsignmentServiceImpl.class.getName());

    private static final int MAX_NOTIFICATION_MSG_LEN = 500;

    @Override
    public long submit(String username, String title, String author, String publisher, String description,
                       int originalPrice, int resalePrice, int conditionPercent, List<String> imageFileNames)
            throws StoreException {
        if (isBlank(username)) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, "Tên tài khoản ký gửi không được để trống");
        }
        validateSubmissionFields(title, author, originalPrice, resalePrice, conditionPercent);
        List<String> cleanImages = sanitizeImagesRequired(imageFileNames);

        String insertBookSql = "INSERT INTO " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " (consignor_username, title, author, publisher, description, original_price, resale_price, condition_percent, status)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBUtil.getConnection()) {
            try {
                con.setAutoCommit(false);

                long usedBookId;
                try (PreparedStatement ps = con.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username.trim());
                    ps.setString(2, title.trim());
                    ps.setString(3, author.trim());
                    ps.setString(4, normalizeOptional(publisher));
                    ps.setString(5, normalizeOptional(description));
                    ps.setInt(6, originalPrice);
                    ps.setInt(7, resalePrice);
                    ps.setInt(8, conditionPercent);
                    ps.setString(9, ConsignmentStatus.PENDING_REVIEW.name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to retrieve generated id for used_books");
                        }
                        usedBookId = keys.getLong(1);
                    }
                }

                insertImages(con, usedBookId, cleanImages);
                insertAdminNotification(con, BookCoinPolicy.NOTIF_CONSIGNMENT_SUBMITTED,
                        String.valueOf(usedBookId),
                        "Yêu cầu ký gửi mới #" + usedBookId + ": " + title.trim() + " (" + username.trim() + ")");

                con.commit();
                return usedBookId;
            } catch (SQLException e) {
                rollbackQuietly(con);
                LOGGER.log(Level.SEVERE, "Error submitting consignment request", e);
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            } finally {
                restoreAutoCommitQuietly(con);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error on consignment submit", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<UsedBook> listByConsignor(String username) throws StoreException {
        if (isBlank(username)) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " WHERE consignor_username = ? ORDER BY created_at DESC, id DESC";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            List<UsedBook> list = readUsedBookList(ps);
            attachImages(con, list);
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing consignments by consignor", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public UsedBook getByIdForOwner(long id, String username) throws StoreException {
        if (id <= 0 || isBlank(username)) {
            return null;
        }
        String sql = "SELECT * FROM " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " WHERE id = ? AND consignor_username = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setString(2, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                UsedBook book = mapUsedBook(rs);
                attachImages(con, Collections.singletonList(book));
                return book;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading consignment by owner", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<UsedBook> listForAdmin(String statusFilter, int limit, int offset) throws StoreException {
        int safeLimit = Math.max(1, limit);
        int safeOffset = Math.max(0, offset);
        boolean hasStatus = !isBlank(statusFilter) && !"ALL".equalsIgnoreCase(statusFilter.trim());
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(ConsignmentDBConstants.TABLE_USED_BOOKS);
        if (hasStatus) {
            sql.append(" WHERE status = ?");
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasStatus) {
                ps.setString(idx++, statusFilter.trim().toUpperCase());
            }
            ps.setInt(idx++, safeLimit);
            ps.setInt(idx, safeOffset);
            List<UsedBook> list = readUsedBookList(ps);
            attachImages(con, list);
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing consignments for admin", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public UsedBook getById(long id) throws StoreException {
        if (id <= 0) {
            return null;
        }
        String sql = "SELECT * FROM " + ConsignmentDBConstants.TABLE_USED_BOOKS + " WHERE id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                UsedBook book = mapUsedBook(rs);
                attachImages(con, Collections.singletonList(book));
                return book;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading consignment by id", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean approve(long id, String adminUsername) throws StoreException {
        if (isBlank(adminUsername)) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, "Thiếu thông tin quản trị viên duyệt yêu cầu");
        }
        String selectForUpdateSql = "SELECT * FROM " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " WHERE id = ? FOR UPDATE";
        String insertBookSql = "INSERT INTO " + BooksDBConstants.TABLE_BOOK
                + " (barcode, name, author, price, quantity, publisher, image_url, description, status)"
                + " VALUES (?, ?, ?, ?, 1, ?, ?, ?, ?)";
        String updateUsedBookSql = "UPDATE " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " SET status = ?, book_barcode = ?, reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP, info_deadline = NULL"
                + " WHERE id = ? AND status IN (?, ?)";

        try (Connection con = DBUtil.getConnection()) {
            try {
                con.setAutoCommit(false);

                UsedBook usedBook;
                try (PreparedStatement ps = con.prepareStatement(selectForUpdateSql)) {
                    ps.setLong(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new StoreException(404, BookCoinPolicy.ERR_NOT_FOUND,
                                    "Không tìm thấy yêu cầu ký gửi #" + id);
                        }
                        usedBook = mapUsedBook(rs);
                    }
                }

                ConsignmentStatus currentStatus = usedBook.getConsignmentStatus();
                if (currentStatus != ConsignmentStatus.PENDING_REVIEW
                        && currentStatus != ConsignmentStatus.NEED_MORE_INFO) {
                    throw new StoreException(409, BookCoinPolicy.ERR_INVALID_STATE,
                            "Chỉ có thể duyệt yêu cầu đang ở trạng thái Chờ duyệt hoặc Cần bổ sung");
                }

                if (usedBook.getConditionPercent() < BookCoinPolicy.MIN_APPROVAL_CONDITION_PERCENT
                        || usedBook.getConditionPercent() > BookCoinPolicy.MAX_APPROVAL_CONDITION_PERCENT) {
                    throw new StoreException(422, BookCoinPolicy.ERR_APPROVAL_RULE,
                            "Độ mới sách phải từ " + BookCoinPolicy.MIN_APPROVAL_CONDITION_PERCENT
                                    + "% đến " + BookCoinPolicy.MAX_APPROVAL_CONDITION_PERCENT + "% để được duyệt");
                }
                if (usedBook.getResalePrice() <= 0 || usedBook.getResalePrice() > usedBook.getOriginalPrice()) {
                    throw new StoreException(422, BookCoinPolicy.ERR_APPROVAL_RULE,
                            "Giá bán lại phải lớn hơn 0 và không vượt quá giá gốc");
                }

                attachImages(con, Collections.singletonList(usedBook));
                if (usedBook.getImageFileNames().isEmpty()) {
                    throw new StoreException(422, BookCoinPolicy.ERR_APPROVAL_RULE,
                            "Yêu cầu ký gửi phải có ít nhất " + BookCoinPolicy.MIN_IMAGES + " ảnh để được duyệt");
                }

                String barcode = BookCoinPolicy.CONSIGNMENT_BARCODE_PREFIX + id;
                String imageUrl = BookCoinPolicy.CONSIGNMENT_IMAGE_URL_PREFIX + usedBook.getImageFileNames().get(0);

                try (PreparedStatement psBook = con.prepareStatement(insertBookSql)) {
                    psBook.setString(1, barcode);
                    psBook.setString(2, usedBook.getTitle());
                    psBook.setString(3, usedBook.getAuthor());
                    psBook.setInt(4, usedBook.getResalePrice());
                    psBook.setString(5, usedBook.getPublisher());
                    psBook.setString(6, imageUrl);
                    psBook.setString(7, usedBook.getDescription());
                    psBook.setString(8, BookCoinPolicy.BOOK_STATUS_ACTIVE);
                    psBook.executeUpdate();
                }

                try (PreparedStatement psUpd = con.prepareStatement(updateUsedBookSql)) {
                    psUpd.setString(1, ConsignmentStatus.LISTED.name());
                    psUpd.setString(2, barcode);
                    psUpd.setString(3, adminUsername.trim());
                    psUpd.setLong(4, id);
                    psUpd.setString(5, ConsignmentStatus.PENDING_REVIEW.name());
                    psUpd.setString(6, ConsignmentStatus.NEED_MORE_INFO.name());
                    if (psUpd.executeUpdate() != 1) {
                        throw new StoreException(409, BookCoinPolicy.ERR_INVALID_STATE,
                                "Trạng thái yêu cầu ký gửi đã thay đổi trước khi duyệt");
                    }
                }

                con.commit();
                return true;
            } catch (StoreException se) {
                rollbackQuietly(con);
                throw se;
            } catch (SQLException e) {
                rollbackQuietly(con);
                LOGGER.log(Level.SEVERE, "Error approving consignment #" + id, e);
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            } finally {
                restoreAutoCommitQuietly(con);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error on consignment approve", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean reject(long id, String adminUsername, String reason) throws StoreException {
        if (isBlank(adminUsername)) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, "Thiếu thông tin quản trị viên");
        }
        String trimmedReason = reason == null ? "" : reason.trim();
        if (trimmedReason.length() < BookCoinPolicy.MIN_ADMIN_NOTE_LENGTH) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Lý do từ chối phải có ít nhất " + BookCoinPolicy.MIN_ADMIN_NOTE_LENGTH + " ký tự");
        }

        String updateSql = "UPDATE " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " SET status = ?, review_note = ?, reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP, info_deadline = NULL"
                + " WHERE id = ? AND status IN (?, ?)";

        try (Connection con = DBUtil.getConnection()) {
            try {
                con.setAutoCommit(false);

                int rows;
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, ConsignmentStatus.REJECTED.name());
                    ps.setString(2, trimmedReason);
                    ps.setString(3, adminUsername.trim());
                    ps.setLong(4, id);
                    ps.setString(5, ConsignmentStatus.PENDING_REVIEW.name());
                    ps.setString(6, ConsignmentStatus.NEED_MORE_INFO.name());
                    rows = ps.executeUpdate();
                }

                if (rows != 1) {
                    throwNotFoundOrInvalidState(con, id, "Không thể từ chối yêu cầu ở trạng thái hiện tại");
                }

                con.commit();
                return true;
            } catch (StoreException se) {
                rollbackQuietly(con);
                throw se;
            } catch (SQLException e) {
                rollbackQuietly(con);
                LOGGER.log(Level.SEVERE, "Error rejecting consignment #" + id, e);
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            } finally {
                restoreAutoCommitQuietly(con);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error on consignment reject", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean requestMoreInfo(long id, String adminUsername, String note) throws StoreException {
        if (isBlank(adminUsername)) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, "Thiếu thông tin quản trị viên");
        }
        String trimmedNote = note == null ? "" : note.trim();
        if (trimmedNote.length() < BookCoinPolicy.MIN_ADMIN_NOTE_LENGTH) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Ghi chú yêu cầu bổ sung phải có ít nhất " + BookCoinPolicy.MIN_ADMIN_NOTE_LENGTH + " ký tự");
        }

        String updateSql = "UPDATE " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " SET status = ?, review_note = ?, reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP,"
                + " info_deadline = TIMESTAMPADD(DAY, ?, CURRENT_TIMESTAMP)"
                + " WHERE id = ? AND status IN (?, ?)";

        try (Connection con = DBUtil.getConnection()) {
            try {
                con.setAutoCommit(false);

                int rows;
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, ConsignmentStatus.NEED_MORE_INFO.name());
                    ps.setString(2, trimmedNote);
                    ps.setString(3, adminUsername.trim());
                    ps.setInt(4, BookCoinPolicy.INFO_RESPONSE_DEADLINE_DAYS);
                    ps.setLong(5, id);
                    ps.setString(6, ConsignmentStatus.PENDING_REVIEW.name());
                    ps.setString(7, ConsignmentStatus.NEED_MORE_INFO.name());
                    rows = ps.executeUpdate();
                }

                if (rows != 1) {
                    throwNotFoundOrInvalidState(con, id,
                            "Không thể yêu cầu bổ sung cho trạng thái hiện tại");
                }

                con.commit();
                return true;
            } catch (StoreException se) {
                rollbackQuietly(con);
                throw se;
            } catch (SQLException e) {
                rollbackQuietly(con);
                LOGGER.log(Level.SEVERE, "Error requesting more info for consignment #" + id, e);
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            } finally {
                restoreAutoCommitQuietly(con);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error on requestMoreInfo", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean respondToInfoRequest(long id, String username, String title, String author, String publisher,
                                        String description, int originalPrice, int resalePrice, int conditionPercent,
                                        List<String> newImageFileNames) throws StoreException {
        if (isBlank(username)) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, "Thiếu thông tin người ký gửi");
        }
        validateSubmissionFields(title, author, originalPrice, resalePrice, conditionPercent);

        List<String> sanitizedNewImages = null;
        if (newImageFileNames != null && !newImageFileNames.isEmpty()) {
            sanitizedNewImages = sanitizeImagesRequired(newImageFileNames);
        }

        String lockSql = "SELECT consignor_username, status, (info_deadline IS NOT NULL AND info_deadline < CURRENT_TIMESTAMP) AS is_overdue"
                + " FROM " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " WHERE id = ? FOR UPDATE";
        String updateSql = "UPDATE " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " SET title = ?, author = ?, publisher = ?, description = ?,"
                + " original_price = ?, resale_price = ?, condition_percent = ?,"
                + " status = ?, info_deadline = NULL"
                + " WHERE id = ? AND consignor_username = ? AND status = ?"
                + " AND (info_deadline IS NULL OR info_deadline >= CURRENT_TIMESTAMP)";

        try (Connection con = DBUtil.getConnection()) {
            try {
                con.setAutoCommit(false);

                String owner;
                String status;
                boolean isOverdue;
                try (PreparedStatement psLock = con.prepareStatement(lockSql)) {
                    psLock.setLong(1, id);
                    try (ResultSet rs = psLock.executeQuery()) {
                        if (!rs.next()) {
                            throw new StoreException(404, BookCoinPolicy.ERR_NOT_FOUND,
                                    "Không tìm thấy yêu cầu ký gửi #" + id);
                        }
                        owner = rs.getString("consignor_username");
                        status = rs.getString("status");
                        isOverdue = rs.getBoolean("is_overdue");
                    }
                }

                if (!username.trim().equalsIgnoreCase(owner)) {
                    throw new StoreException(403, BookCoinPolicy.ERR_FORBIDDEN,
                            "Bạn không có quyền cập nhật yêu cầu ký gửi này");
                }
                if (!ConsignmentStatus.NEED_MORE_INFO.name().equalsIgnoreCase(status)) {
                    throw new StoreException(409, BookCoinPolicy.ERR_INVALID_STATE,
                            "Chỉ có thể phản hồi khi yêu cầu đang ở trạng thái Cần bổ sung thông tin");
                }
                if (isOverdue) {
                    throw new StoreException(409, BookCoinPolicy.ERR_DEADLINE_EXPIRED,
                            "Yêu cầu ký gửi đã quá thời hạn bổ sung thông tin");
                }

                if (sanitizedNewImages != null) {
                    try (PreparedStatement psDel = con.prepareStatement(
                            "DELETE FROM " + ConsignmentDBConstants.TABLE_USED_BOOK_IMAGES + " WHERE used_book_id = ?")) {
                        psDel.setLong(1, id);
                        psDel.executeUpdate();
                    }
                    insertImages(con, id, sanitizedNewImages);
                } else {
                    int existingImgCount = countImages(con, id);
                    if (existingImgCount < BookCoinPolicy.MIN_IMAGES) {
                        throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                                "Yêu cầu ký gửi phải có ít nhất " + BookCoinPolicy.MIN_IMAGES + " ảnh");
                    }
                }

                try (PreparedStatement psUpd = con.prepareStatement(updateSql)) {
                    psUpd.setString(1, title.trim());
                    psUpd.setString(2, author.trim());
                    psUpd.setString(3, normalizeOptional(publisher));
                    psUpd.setString(4, normalizeOptional(description));
                    psUpd.setInt(5, originalPrice);
                    psUpd.setInt(6, resalePrice);
                    psUpd.setInt(7, conditionPercent);
                    psUpd.setString(8, ConsignmentStatus.PENDING_REVIEW.name());
                    psUpd.setLong(9, id);
                    psUpd.setString(10, owner);
                    psUpd.setString(11, ConsignmentStatus.NEED_MORE_INFO.name());
                    if (psUpd.executeUpdate() != 1) {
                        throw new StoreException(409, BookCoinPolicy.ERR_INVALID_STATE,
                                "Không thể cập nhật phản hồi bổ sung thông tin");
                    }
                }

                insertAdminNotification(con, BookCoinPolicy.NOTIF_CONSIGNMENT_RESUBMITTED,
                        String.valueOf(id),
                        "Khách hàng " + owner + " đã bổ sung thông tin cho ký gửi #" + id);

                con.commit();
                return true;
            } catch (StoreException se) {
                rollbackQuietly(con);
                throw se;
            } catch (SQLException e) {
                rollbackQuietly(con);
                LOGGER.log(Level.SEVERE, "Error responding to info request #" + id, e);
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            } finally {
                restoreAutoCommitQuietly(con);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error on respondToInfoRequest", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public int expireOverdueInfoRequests() throws StoreException {
        String sql = "UPDATE " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " SET status = ?, review_note = ?, reviewed_at = CURRENT_TIMESTAMP"
                + " WHERE status = ? AND info_deadline IS NOT NULL AND info_deadline < CURRENT_TIMESTAMP";
        String overdueNote = "Tự động từ chối do quá hạn bổ sung thông tin ("
                + BookCoinPolicy.INFO_RESPONSE_DEADLINE_DAYS + " ngày)";

        try (Connection con = DBUtil.getConnection()) {
            try {
                con.setAutoCommit(false);
                int expiredCount;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, ConsignmentStatus.REJECTED.name());
                    ps.setString(2, overdueNote);
                    ps.setString(3, ConsignmentStatus.NEED_MORE_INFO.name());
                    expiredCount = ps.executeUpdate();
                }
                con.commit();
                return expiredCount;
            } catch (SQLException e) {
                rollbackQuietly(con);
                LOGGER.log(Level.SEVERE, "Error expiring overdue consignment info requests", e);
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            } finally {
                restoreAutoCommitQuietly(con);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error on expireOverdueInfoRequests", e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public int markSoldForOrder(Connection con, String orderId) throws StoreException {
        if (con == null) {
            throw new StoreException(ResponseCode.BAD_REQUEST);
        }
        if (isBlank(orderId)) {
            return 0;
        }
        String normalizedOrderId = orderId.trim();
        String sql = "UPDATE " + ConsignmentDBConstants.TABLE_USED_BOOKS + " ub"
                + " INNER JOIN order_details od ON ub.book_barcode = od.book_barcode"
                + " SET ub.status = ?, ub.sold_order_id = ?, ub.sold_at = CURRENT_TIMESTAMP"
                + " WHERE od.order_id = ? AND ub.status = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ConsignmentStatus.SOLD.name());
            ps.setString(2, normalizedOrderId);
            ps.setString(3, normalizedOrderId);
            ps.setString(4, ConsignmentStatus.LISTED.name());
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking used_books sold for order " + normalizedOrderId, e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public int relistForOrder(Connection con, String orderId) throws StoreException {
        if (con == null) {
            throw new StoreException(ResponseCode.BAD_REQUEST);
        }
        if (isBlank(orderId)) {
            return 0;
        }
        String normalizedOrderId = orderId.trim();
        String sql = "UPDATE " + ConsignmentDBConstants.TABLE_USED_BOOKS
                + " SET status = ?, sold_order_id = NULL, sold_at = NULL"
                + " WHERE sold_order_id = ? AND status = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ConsignmentStatus.LISTED.name());
            ps.setString(2, normalizedOrderId);
            ps.setString(3, ConsignmentStatus.SOLD.name());
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error relisting used_books for order " + normalizedOrderId, e);
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateSubmissionFields(String title, String author, int originalPrice, int resalePrice,
                                          int conditionPercent) throws StoreException {
        if (isBlank(title) || isBlank(author)) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, "Tên sách và tác giả là bắt buộc");
        }
        if (originalPrice <= 0 || resalePrice <= 0) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Giá bìa gốc và giá bán lại phải lớn hơn 0");
        }
        if (conditionPercent < BookCoinPolicy.MIN_SUBMIT_CONDITION_PERCENT
                || conditionPercent > BookCoinPolicy.MAX_SUBMIT_CONDITION_PERCENT) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Độ mới sách phải nằm trong khoảng " + BookCoinPolicy.MIN_SUBMIT_CONDITION_PERCENT
                            + "–" + BookCoinPolicy.MAX_SUBMIT_CONDITION_PERCENT + "%");
        }
    }

    private List<String> sanitizeImagesRequired(List<String> imageFileNames) throws StoreException {
        List<String> clean = new ArrayList<>();
        if (imageFileNames != null) {
            for (String name : imageFileNames) {
                if (!isBlank(name)) {
                    clean.add(name.trim());
                }
            }
        }
        if (clean.size() < BookCoinPolicy.MIN_IMAGES || clean.size() > BookCoinPolicy.MAX_IMAGES) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Số lượng ảnh sách ký gửi phải từ " + BookCoinPolicy.MIN_IMAGES
                            + " đến " + BookCoinPolicy.MAX_IMAGES + " ảnh");
        }
        return clean;
    }

    private void insertImages(Connection con, long usedBookId, List<String> imageFileNames) throws SQLException {
        String sql = "INSERT INTO " + ConsignmentDBConstants.TABLE_USED_BOOK_IMAGES
                + " (used_book_id, file_name, sort_order) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < imageFileNames.size(); i++) {
                ps.setLong(1, usedBookId);
                ps.setString(2, imageFileNames.get(i));
                ps.setInt(3, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private int countImages(Connection con, long usedBookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + ConsignmentDBConstants.TABLE_USED_BOOK_IMAGES
                + " WHERE used_book_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usedBookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void insertAdminNotification(Connection con, String type, String refId, String message)
            throws SQLException {
        String safeMsg = message == null ? "" : message;
        if (safeMsg.length() > MAX_NOTIFICATION_MSG_LEN) {
            safeMsg = safeMsg.substring(0, MAX_NOTIFICATION_MSG_LEN);
        }
        String sql = "INSERT INTO " + ConsignmentDBConstants.TABLE_ADMIN_NOTIFICATIONS
                + " (type, ref_id, message) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, refId);
            ps.setString(3, safeMsg);
            ps.executeUpdate();
        }
    }

    private void throwNotFoundOrInvalidState(Connection con, long id, String invalidStateMessage)
            throws SQLException, StoreException {
        String checkSql = "SELECT status FROM " + ConsignmentDBConstants.TABLE_USED_BOOKS + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new StoreException(404, BookCoinPolicy.ERR_NOT_FOUND,
                            "Không tìm thấy yêu cầu ký gửi #" + id);
                }
            }
        }
        throw new StoreException(409, BookCoinPolicy.ERR_INVALID_STATE, invalidStateMessage);
    }

    private List<UsedBook> readUsedBookList(PreparedStatement ps) throws SQLException {
        List<UsedBook> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUsedBook(rs));
            }
        }
        return list;
    }

    private void attachImages(Connection con, List<UsedBook> books) throws SQLException {
        if (books == null || books.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", Collections.nCopies(books.size(), "?"));
        String sql = "SELECT used_book_id, file_name FROM " + ConsignmentDBConstants.TABLE_USED_BOOK_IMAGES
                + " WHERE used_book_id IN (" + placeholders + ")"
                + " ORDER BY used_book_id ASC, sort_order ASC, id ASC";

        Map<Long, List<String>> imageMap = new HashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < books.size(); i++) {
                ps.setLong(i + 1, books.get(i).getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long bookId = rs.getLong("used_book_id");
                    imageMap.computeIfAbsent(bookId, k -> new ArrayList<>()).add(rs.getString("file_name"));
                }
            }
        }
        for (UsedBook book : books) {
            List<String> files = imageMap.get(book.getId());
            book.setImageFileNames(files != null ? files : Collections.emptyList());
        }
    }

    private UsedBook mapUsedBook(ResultSet rs) throws SQLException {
        UsedBook book = new UsedBook();
        book.setId(rs.getLong("id"));
        book.setConsignorUsername(rs.getString("consignor_username"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setDescription(rs.getString("description"));
        book.setOriginalPrice(rs.getInt("original_price"));
        book.setResalePrice(rs.getInt("resale_price"));
        book.setConditionPercent(rs.getInt("condition_percent"));
        book.setStatus(rs.getString("status"));
        book.setReviewNote(rs.getString("review_note"));
        book.setReviewedBy(rs.getString("reviewed_by"));
        book.setReviewedAt(rs.getTimestamp("reviewed_at"));
        book.setInfoDeadline(rs.getTimestamp("info_deadline"));
        book.setBookBarcode(rs.getString("book_barcode"));
        book.setSoldOrderId(rs.getString("sold_order_id"));
        book.setSoldAt(rs.getTimestamp("sold_at"));
        book.setCreatedAt(rs.getTimestamp("created_at"));
        book.setUpdatedAt(rs.getTimestamp("updated_at"));
        return book;
    }

    private void rollbackQuietly(Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.rollback();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Rollback failed", ex);
        }
    }

    private void restoreAutoCommitQuietly(Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to restore autoCommit=true", ex);
        }
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
