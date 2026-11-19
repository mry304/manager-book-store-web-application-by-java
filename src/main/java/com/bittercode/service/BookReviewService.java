package com.bittercode.service;

import java.util.List;

import com.bittercode.model.BookReview;
import com.bittercode.model.StoreException;

/**
 * Service cho chức năng Đánh giá sách (UC04) + Quản lý đánh giá Admin.
 */
public interface BookReviewService {

    /** Lấy tất cả đánh giá VISIBLE của một cuốn sách, mới nhất lên đầu, kèm admin_reply */
    List<BookReview> getReviewsByBarcode(String barcode) throws StoreException;

    /** Tính điểm sao trung bình (0.0 nếu chưa có đánh giá) */
    double getAverageRating(String barcode) throws StoreException;

    /** Đếm tổng số lượt đánh giá */
    int getReviewCount(String barcode) throws StoreException;

    /** Thêm một đánh giá mới */
    String addReview(BookReview review) throws StoreException;

    /** Lấy toàn bộ đánh giá cho trang Admin, hỗ trợ lọc */
    List<BookReview> getAllReviewsForAdmin(String keyword, String ratingFilter, String replyStatus) throws StoreException;

    /** Admin trả lời hoặc sửa phản hồi */
    String replyToReview(int reviewId, String replyContent, String adminUsername) throws StoreException;

    /** Admin ẩn/hiện đánh giá */
    String toggleReviewStatus(int reviewId, String newStatus) throws StoreException;

    /** Admin xóa đánh giá */
    String deleteReview(int reviewId) throws StoreException;
}
