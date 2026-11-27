package com.bittercode.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO ánh xạ bảng used_books và danh sách ảnh đi kèm từ used_book_images.
 */
public class UsedBook implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String consignorUsername;
    private String title;
    private String author;
    private String publisher;
    private String description;
    private int originalPrice;
    private int resalePrice;
    private int conditionPercent;
    private String status;
    private String reviewNote;
    private String reviewedBy;
    private Timestamp reviewedAt;
    private Timestamp infoDeadline;
    private String bookBarcode;
    private String soldOrderId;
    private Timestamp soldAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<String> imageFileNames = new ArrayList<>();

    public UsedBook() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getConsignorUsername() {
        return consignorUsername;
    }

    public void setConsignorUsername(String consignorUsername) {
        this.consignorUsername = consignorUsername;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(int originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getResalePrice() {
        return resalePrice;
    }

    public void setResalePrice(int resalePrice) {
        this.resalePrice = resalePrice;
    }

    public int getConditionPercent() {
        return conditionPercent;
    }

    public void setConditionPercent(int conditionPercent) {
        this.conditionPercent = conditionPercent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ConsignmentStatus getConsignmentStatus() {
        return ConsignmentStatus.fromString(this.status);
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Timestamp getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Timestamp reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Timestamp getInfoDeadline() {
        return infoDeadline;
    }

    public void setInfoDeadline(Timestamp infoDeadline) {
        this.infoDeadline = infoDeadline;
    }

    public String getBookBarcode() {
        return bookBarcode;
    }

    public void setBookBarcode(String bookBarcode) {
        this.bookBarcode = bookBarcode;
    }

    public String getSoldOrderId() {
        return soldOrderId;
    }

    public void setSoldOrderId(String soldOrderId) {
        this.soldOrderId = soldOrderId;
    }

    public Timestamp getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(Timestamp soldAt) {
        this.soldAt = soldAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getImageFileNames() {
        return imageFileNames;
    }

    public void setImageFileNames(List<String> imageFileNames) {
        this.imageFileNames = imageFileNames != null ? new ArrayList<>(imageFileNames) : new ArrayList<>();
    }
}
