package com.bittercode.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bittercode.constant.ResponseCode;
import com.bittercode.model.BookReview;
import com.bittercode.model.StoreException;
import com.bittercode.service.BookReviewService;
import com.bittercode.util.DBUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Triển khai BookReviewService — thao tác bảng book_reviews (UC04 + Admin review management).
 */
public class BookReviewServiceImpl implements BookReviewService {
    private static final Logger LOGGER = Logger.getLogger(BookReviewServiceImpl.class.getName());


    // Lấy danh sách đánh giá VISIBLE kèm admin_reply, mới nhất lên đầu
    private static final String GET_REVIEWS_SQL =
            "SELECT br.id, br.book_barcode, br.username, br.rating, br.comment, br.created_at, "
          + "br.admin_reply, br.replied_by, br.replied_at, br.status, "
          + "CONCAT(u.firstname, ' ', u.lastname) AS user_fullname "
          + "FROM book_reviews br "
          + "LEFT JOIN users u ON br.username = u.username "
          + "WHERE br.book_barcode = ? AND (br.status = 'VISIBLE' OR br.status IS NULL) "
          + "ORDER BY br.created_at DESC";

    private static final String AVG_RATING_SQL =
            "SELECT AVG(rating) FROM book_reviews WHERE book_barcode = ? AND (status = 'VISIBLE' OR status IS NULL)";

    private static final String COUNT_REVIEWS_SQL =
            "SELECT COUNT(*) FROM book_reviews WHERE book_barcode = ? AND (status = 'VISIBLE' OR status IS NULL)";

    private static final String ADD_REVIEW_SQL =
            "INSERT INTO book_reviews (book_barcode, username, rating, comment) VALUES (?, ?, ?, ?)";

    private static final String REPLY_SQL =
            "UPDATE book_reviews SET admin_reply = ?, replied_by = ?, replied_at = NOW() WHERE id = ?";

    private static final String TOGGLE_STATUS_SQL =
            "UPDATE book_reviews SET status = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM book_reviews WHERE id = ?";

    @Override
    public List<BookReview> getReviewsByBarcode(String barcode) throws StoreException {
        List<BookReview> reviews = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_REVIEWS_SQL)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapReviewRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return reviews;
    }

    @Override
    public double getAverageRating(String barcode) throws StoreException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(AVG_RATING_SQL)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : avg;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return 0.0;
    }

    @Override
    public int getReviewCount(String barcode) throws StoreException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(COUNT_REVIEWS_SQL)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public String addReview(BookReview review) throws StoreException {
        String responseCode = ResponseCode.FAILURE.name();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(ADD_REVIEW_SQL)) {
            ps.setString(1, review.getBookBarcode());
            ps.setString(2, review.getUsername());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            int rows = ps.executeUpdate();
            if (rows == 1) {
                responseCode = ResponseCode.SUCCESS.name();
            }
        } catch (SQLException e) {
            responseCode += " : " + e.getMessage();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return responseCode;
    }

    @Override
    public List<BookReview> getAllReviewsForAdmin(String keyword, String ratingFilter, String replyStatus)
            throws StoreException {
        List<BookReview> reviews = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT br.id, br.book_barcode, br.username, br.rating, br.comment, br.created_at, ");
        sql.append("br.admin_reply, br.replied_by, br.replied_at, br.status, ");
        sql.append("CONCAT(u.firstname, ' ', u.lastname) AS user_fullname, ");
        sql.append("b.name AS book_name, b.image_url AS book_image_url ");
        sql.append("FROM book_reviews br ");
        sql.append("LEFT JOIN users u ON br.username = u.username ");
        sql.append("LEFT JOIN books b ON br.book_barcode = b.barcode ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // Keyword search
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim() + "%";
            sql.append("AND (b.name LIKE ? OR br.book_barcode LIKE ? OR br.username LIKE ? OR br.comment LIKE ?) ");
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        // Rating filter
        if (ratingFilter != null && !ratingFilter.trim().isEmpty()) {
            switch (ratingFilter) {
                case "5":
                    sql.append("AND br.rating = 5 ");
                    break;
                case "4":
                    sql.append("AND br.rating = 4 ");
                    break;
                case "3":
                    sql.append("AND br.rating = 3 ");
                    break;
                case "1-2":
                    sql.append("AND br.rating <= 2 ");
                    break;
            }
        }

        // Reply status filter
        if (replyStatus != null && !replyStatus.trim().isEmpty()) {
            switch (replyStatus) {
                case "unreplied":
                    sql.append("AND (br.admin_reply IS NULL OR br.admin_reply = '') ");
                    break;
                case "replied":
                    sql.append("AND br.admin_reply IS NOT NULL AND br.admin_reply != '' ");
                    break;
            }
        }

        sql.append("ORDER BY br.created_at DESC");

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookReview r = mapReviewRow(rs);
                    // Extra fields for admin
                    try { r.setBookName(rs.getString("book_name")); } catch (SQLException ignored) {}
                    try { r.setBookImageUrl(rs.getString("book_image_url")); } catch (SQLException ignored) {}
                    reviews.add(r);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return reviews;
    }

    @Override
    public String replyToReview(int reviewId, String replyContent, String adminUsername) throws StoreException {
        String responseCode = ResponseCode.FAILURE.name();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(REPLY_SQL)) {
            ps.setString(1, replyContent);
            ps.setString(2, adminUsername);
            ps.setInt(3, reviewId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                responseCode = ResponseCode.SUCCESS.name();
            }
        } catch (SQLException e) {
            responseCode += " : " + e.getMessage();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return responseCode;
    }

    @Override
    public String toggleReviewStatus(int reviewId, String newStatus) throws StoreException {
        String responseCode = ResponseCode.FAILURE.name();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(TOGGLE_STATUS_SQL)) {
            ps.setString(1, newStatus);
            ps.setInt(2, reviewId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                responseCode = ResponseCode.SUCCESS.name();
            }
        } catch (SQLException e) {
            responseCode += " : " + e.getMessage();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return responseCode;
    }

    @Override
    public String deleteReview(int reviewId) throws StoreException {
        String responseCode = ResponseCode.FAILURE.name();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, reviewId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                responseCode = ResponseCode.SUCCESS.name();
            }
        } catch (SQLException e) {
            responseCode += " : " + e.getMessage();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return responseCode;
    }

    // --- Helper: map a ResultSet row to BookReview ---
    private BookReview mapReviewRow(ResultSet rs) throws SQLException {
        BookReview r = new BookReview();
        r.setId(rs.getInt("id"));
        r.setBookBarcode(rs.getString("book_barcode"));
        r.setUsername(rs.getString("username"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        try { r.setUserFullName(rs.getString("user_fullname")); } catch (SQLException ignored) {}
        try { r.setAdminReply(rs.getString("admin_reply")); } catch (SQLException ignored) {}
        try { r.setRepliedBy(rs.getString("replied_by")); } catch (SQLException ignored) {}
        try { r.setRepliedAt(rs.getTimestamp("replied_at")); } catch (SQLException ignored) {}
        try { r.setStatus(rs.getString("status")); } catch (SQLException ignored) {}
        return r;
    }
}
