package com.bittercode.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model đánh giá / nhận xét sách (UC04).
 * Quan hệ N-1 với bảng books (book_barcode) và users (username).
 */
public class BookReview implements Serializable {

    private int id;
    private String bookBarcode;
    private String username;
    private int rating;        // 1 – 5
    private String comment;
    private Timestamp createdAt;

    // Trường phụ dùng cho hiển thị — không lưu DB
    private String userFullName;

    // --- Admin reply fields ---
    private String adminReply;
    private String repliedBy;
    private Timestamp repliedAt;
    private String status;      // VISIBLE / HIDDEN

    // --- Extra fields for admin list view (JOIN from books) ---
    private String bookName;
    private String bookImageUrl;

    public BookReview() {
    }

    // --- Getter / Setter ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookBarcode() {
        return bookBarcode;
    }

    public void setBookBarcode(String bookBarcode) {
        this.bookBarcode = bookBarcode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public String getRepliedBy() {
        return repliedBy;
    }

    public void setRepliedBy(String repliedBy) {
        this.repliedBy = repliedBy;
    }

    public Timestamp getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(Timestamp repliedAt) {
        this.repliedAt = repliedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookImageUrl() {
        return bookImageUrl;
    }

    public void setBookImageUrl(String bookImageUrl) {
        this.bookImageUrl = bookImageUrl;
    }
}
