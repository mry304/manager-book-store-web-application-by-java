package com.bittercode.model;

/**
 * Loại giao dịch trong sổ cái Book Coins (bảng coin_transactions).
 */
public enum CoinTransactionType {
    CONSIGN_REWARD("Thưởng ký gửi sách cũ"),
    ORDER_REDEEM("Sử dụng Book Coins thanh toán đơn hàng"),
    ORDER_REFUND("Hoàn Book Coins do hủy/hết hạn đơn hàng");

    private final String displayName;

    CoinTransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CoinTransactionType fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        try {
            return CoinTransactionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
