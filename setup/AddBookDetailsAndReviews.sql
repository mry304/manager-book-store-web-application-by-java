-- ============================================================
-- Script: AddBookDetailsAndReviews.sql
-- Mục đích: Bổ sung cột chi tiết cho bảng books (UC03)
--           và tạo bảng book_reviews (UC04)
-- Database: onlinebookstore
-- ============================================================

USE onlinebookstore;

-- 1. Bổ sung các cột chi tiết cho bảng books
--    MySQL 8.x không hỗ trợ ADD COLUMN IF NOT EXISTS trực tiếp,
--    nên sử dụng Stored Procedure an toàn.

DELIMITER //

DROP PROCEDURE IF EXISTS add_book_detail_columns //

CREATE PROCEDURE add_book_detail_columns()
BEGIN
    -- publisher
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'books' AND COLUMN_NAME = 'publisher'
    ) THEN
        ALTER TABLE books ADD COLUMN publisher VARCHAR(200) DEFAULT NULL;
    END IF;

    -- image_url
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'books' AND COLUMN_NAME = 'image_url'
    ) THEN
        ALTER TABLE books ADD COLUMN image_url VARCHAR(500) DEFAULT NULL;
    END IF;

    -- description
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'books' AND COLUMN_NAME = 'description'
    ) THEN
        ALTER TABLE books ADD COLUMN description TEXT DEFAULT NULL;
    END IF;

    -- category_id
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'books' AND COLUMN_NAME = 'category_id'
    ) THEN
        ALTER TABLE books ADD COLUMN category_id INT DEFAULT NULL;
    END IF;

    -- status
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'books' AND COLUMN_NAME = 'status'
    ) THEN
        ALTER TABLE books ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE';
    END IF;
END //

DELIMITER ;

CALL add_book_detail_columns();
DROP PROCEDURE IF EXISTS add_book_detail_columns;

-- 2. Tạo bảng book_reviews (đánh giá sách - quan hệ 1-N với books và users)
CREATE TABLE IF NOT EXISTS book_reviews (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    book_barcode VARCHAR(100) NOT NULL,
    username    VARCHAR(100) NOT NULL,
    rating      INT          NOT NULL,
    comment     TEXT         NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT fk_review_book FOREIGN KEY (book_barcode) REFERENCES books(barcode) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (username)     REFERENCES users(username) ON DELETE CASCADE,

    INDEX idx_reviews_barcode  (book_barcode),
    INDEX idx_reviews_username (username),
    INDEX idx_reviews_created  (created_at DESC)
);

-- 3. Bổ sung các cột Admin reply cho bảng book_reviews (Quản lý đánh giá)
--    Chạy từng câu ALTER riêng trong phpMyAdmin.

-- Bước 3a: Thêm cột admin_reply
-- ALTER TABLE book_reviews ADD COLUMN admin_reply TEXT DEFAULT NULL;

-- Bước 3b: Thêm cột replied_by
-- ALTER TABLE book_reviews ADD COLUMN replied_by VARCHAR(100) DEFAULT NULL;

-- Bước 3c: Thêm cột replied_at
-- ALTER TABLE book_reviews ADD COLUMN replied_at DATETIME DEFAULT NULL;

-- Bước 3d: Thêm cột status
-- ALTER TABLE book_reviews ADD COLUMN status VARCHAR(20) DEFAULT 'VISIBLE';
