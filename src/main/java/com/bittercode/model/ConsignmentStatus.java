package com.bittercode.model;

/**
 * Trạng thái vòng đời của yêu cầu ký gửi / sách cũ (bảng used_books).
 */
public enum ConsignmentStatus {
    PENDING_REVIEW("Chờ duyệt", "badge-order-pending"),
    NEED_MORE_INFO("Cần bổ sung thông tin", "badge-order-shipping"),
    REJECTED("Từ chối", "badge-order-cancelled"),
    LISTED("Đang bán", "badge-order-confirmed"),
    SOLD("Đã bán", "badge-order-completed");

    private final String displayName;
    private final String badgeClass;

    ConsignmentStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public static ConsignmentStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return ConsignmentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
