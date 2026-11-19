package com.bittercode.model;

import java.io.Serializable;

public class Book implements Serializable {

    private String barcode;
    private String name;
    private String author;
    private double price;
    private int quantity;

    // --- Các trường bổ sung cho UC03 (Xem chi tiết sách) ---
    private String publisher;
    private String imageUrl;
    private String description;
    private int categoryId;
    private String status;

    public Book(String barcode, String name, String author, double price, int quantity) {
        this.barcode = barcode;
        this.name = name;
        this.author = author;
        this.setPrice(price);
        this.quantity = quantity;
    }

    public Book() {
        super();
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // --- Getter / Setter cho các trường bổ sung ---

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
