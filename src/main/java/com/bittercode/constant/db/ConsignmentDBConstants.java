package com.bittercode.constant.db;

/**
 * Tên bảng và tên cột cho chức năng Ký gửi sách cũ & Book Coins.
 */
public interface ConsignmentDBConstants {

    String TABLE_USED_BOOKS = "used_books";
    String TABLE_USED_BOOK_IMAGES = "used_book_images";
    String TABLE_COIN_TRANSACTIONS = "coin_transactions";
    String TABLE_ADMIN_NOTIFICATIONS = "admin_notifications";

    String COL_USER_BOOK_COINS = "book_coins";
    String COL_ORDER_COINS_USED = "coins_used";
}
