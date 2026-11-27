package com.bittercode.constant;

/**
 * Hằng số chính sách Book Coins và quy trình Ký gửi sách cũ.
 * Toàn bộ tính toán sử dụng số nguyên (int/long), không dùng double.
 */
public final class BookCoinPolicy {

    private BookCoinPolicy() {
    }

    /** 1 Book Coin = 1 VNĐ */
    public static final long COIN_VALUE_VND = 1L;

    /** Trần giảm tối đa bằng Book Coins: 50% tiền hàng */
    public static final int MAX_REDEEM_PERCENT = 50;

    /** Số tiền tối thiểu còn phải thanh toán sau khi trừ Book Coins (VNĐ) */
    public static final long MIN_PAYABLE_VND = 1000L;

    /** Tỷ lệ thưởng ký gửi: 10% giá bán lại (chia nguyên / 10) */
    public static final int CONSIGNMENT_REWARD_PERCENT = 10;
    public static final int CONSIGNMENT_REWARD_DIVISOR = 10;

    /** Thời hạn bổ sung thông tin ký gửi (ngày) */
    public static final int INFO_RESPONSE_DEADLINE_DAYS = 7;

    /** Ràng buộc dữ liệu khi khách gửi/bổ sung yêu cầu ký gửi */
    public static final int MIN_SUBMIT_CONDITION_PERCENT = 1;
    public static final int MAX_SUBMIT_CONDITION_PERCENT = 100;
    public static final int MIN_IMAGES = 1;
    public static final int MAX_IMAGES = 3;

    /** Điều kiện duyệt ký gửi và độ dài lý do/ghi chú của quản trị viên */
    public static final int MIN_APPROVAL_CONDITION_PERCENT = 70;
    public static final int MAX_APPROVAL_CONDITION_PERCENT = 100;
    public static final int MIN_ADMIN_NOTE_LENGTH = 10;

    /** Quy ước sinh mã barcode & URL ảnh sách cũ khi lên kệ */
    public static final String CONSIGNMENT_BARCODE_PREFIX = "UB-";
    public static final String CONSIGNMENT_IMAGE_URL_PREFIX = "consignment-images/";
    public static final String BOOK_STATUS_ACTIVE = "ACTIVE";

    /** Loại thông báo admin */
    public static final String NOTIF_CONSIGNMENT_SUBMITTED = "CONSIGNMENT_SUBMITTED";
    public static final String NOTIF_CONSIGNMENT_RESUBMITTED = "CONSIGNMENT_RESUBMITTED";

    /** Mã lỗi chi tiết dùng trong StoreException.errorCode */
    public static final String ERR_VALIDATION = "VALIDATION_ERROR";
    public static final String ERR_NOT_FOUND = "NOT_FOUND";
    public static final String ERR_FORBIDDEN = "FORBIDDEN";
    public static final String ERR_INVALID_STATE = "INVALID_STATE";
    public static final String ERR_APPROVAL_RULE = "APPROVAL_RULE_VIOLATION";
    public static final String ERR_DEADLINE_EXPIRED = "DEADLINE_EXPIRED";
}
