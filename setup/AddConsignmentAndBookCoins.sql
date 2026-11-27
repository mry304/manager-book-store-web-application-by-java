-- ============================================================
-- Migration: AddConsignmentAndBookCoins.sql
-- Mục đích : Giai đoạn 1 - Tạo schema cho tính năng
--            "Ký gửi sách cũ và quản lý Book Coins"
-- Database : onlinebookstore
-- Phiên bản giả định: MySQL 8.x (>= 8.0.16, tương thích driver mysql-connector-java 8.0.28)
-- Lưu ý    :
--   - MySQL 8.0.16+ hỗ trợ và thực thi đầy đủ CHECK constraint.
--   - MySQL 8.x không hỗ trợ ALTER TABLE ... ADD COLUMN IF NOT EXISTS,
--     nên dùng Stored Procedure tạm kiểm tra information_schema (idempotent).
--   - Không chỉ định ENGINE / CHARSET / COLLATE tường minh để kế thừa đúng
--     cấu hình mặc định của database onlinebookstore như setup/CreateDatastore.sql.
-- ============================================================

USE onlinebookstore;

-- 1. Bảng used_books (yêu cầu ký gửi và bản ghi sách cũ)
--    Không đặt CHECK cho condition_percent / resale_price vì đây là điều kiện duyệt ở tầng service.
CREATE TABLE IF NOT EXISTS used_books (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    consignor_username VARCHAR(100) NOT NULL,
    title              VARCHAR(255) NOT NULL,
    author             VARCHAR(100) NOT NULL,
    publisher          VARCHAR(200) NULL,
    description        TEXT NULL,
    original_price     INT NOT NULL,
    resale_price       INT NOT NULL,
    condition_percent  TINYINT NOT NULL,
    status             VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
    review_note        TEXT NULL,
    reviewed_by        VARCHAR(100) NULL,
    reviewed_at        TIMESTAMP NULL,
    info_deadline      TIMESTAMP NULL,
    book_barcode       VARCHAR(100) NULL UNIQUE,
    sold_order_id      VARCHAR(100) NULL,
    sold_at            TIMESTAMP NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_used_books_consignor FOREIGN KEY (consignor_username) REFERENCES users(username),
    CONSTRAINT fk_used_books_book      FOREIGN KEY (book_barcode)       REFERENCES books(barcode),

    INDEX idx_used_books_consignor_status (consignor_username, status),
    INDEX idx_used_books_status_created    (status, created_at)
);

-- 2. Bảng used_book_images (ảnh sách ký gửi)
CREATE TABLE IF NOT EXISTS used_book_images (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    used_book_id BIGINT NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    sort_order   TINYINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_used_book_images_book FOREIGN KEY (used_book_id) REFERENCES used_books(id) ON DELETE CASCADE,

    INDEX idx_used_book_images_book_sort (used_book_id, sort_order)
);

-- 3. Bảng coin_transactions (sổ cái giao dịch Book Coins)
CREATE TABLE IF NOT EXISTS coin_transactions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL,
    amount        INT NOT NULL,
    type          VARCHAR(30) NOT NULL,
    ref_id        VARCHAR(100) NOT NULL,
    order_id      VARCHAR(100) NULL,
    balance_after BIGINT NOT NULL,
    note          VARCHAR(255) NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_coin_transactions_user FOREIGN KEY (username) REFERENCES users(username),
    UNIQUE KEY uq_coin_type_ref (type, ref_id),
    INDEX idx_coin_transactions_user_created (username, created_at)
);

-- 4. Bảng admin_notifications (thông báo cho quản trị viên)
CREATE TABLE IF NOT EXISTS admin_notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    type       VARCHAR(50) NOT NULL,
    ref_id     VARCHAR(100) NULL,
    message    VARCHAR(500) NOT NULL,
    is_read    TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_admin_notifications_read_created (is_read, created_at)
);

-- 5 & 6. Bổ sung cột cho users và orders một cách idempotent qua information_schema
DROP PROCEDURE IF EXISTS migrate_consignment_and_book_coins;
DELIMITER //

CREATE PROCEDURE migrate_consignment_and_book_coins()
BEGIN
    -- 5a. Thêm cột users.book_coins nếu chưa tồn tại
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND column_name = 'book_coins'
    ) THEN
        ALTER TABLE users
            ADD COLUMN book_coins BIGINT NOT NULL DEFAULT 0;
    END IF;

    -- 5b. Thêm CHECK (book_coins >= 0) trên bảng users (thực thi từ MySQL 8.0.16+)
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND constraint_name = 'chk_users_book_coins'
          AND constraint_type = 'CHECK'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT chk_users_book_coins CHECK (book_coins >= 0);
    END IF;

    -- 6. Thêm cột orders.coins_used nếu chưa tồn tại
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'orders'
          AND column_name = 'coins_used'
    ) THEN
        ALTER TABLE orders
            ADD COLUMN coins_used INT NOT NULL DEFAULT 0;
    END IF;
END //

DELIMITER ;
CALL migrate_consignment_and_book_coins();
DROP PROCEDURE IF EXISTS migrate_consignment_and_book_coins;
