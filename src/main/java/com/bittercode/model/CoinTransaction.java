package com.bittercode.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * POJO ánh xạ bảng coin_transactions (sổ cái giao dịch Book Coins).
 */
public class CoinTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String username;
    private int amount;
    private String type;
    private String refId;
    private String orderId;
    private long balanceAfter;
    private String note;
    private Timestamp createdAt;

    public CoinTransaction() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CoinTransactionType getTransactionType() {
        return CoinTransactionType.fromString(this.type);
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(long balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
