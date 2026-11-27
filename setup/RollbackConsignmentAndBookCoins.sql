-- ============================================================
-- Rollback: RollbackConsignmentAndBookCoins.sql
-- Mục đích: Hoàn tác toàn bộ thay đổi của AddConsignmentAndBookCoins.sql
--           một cách an toàn và chạy lại được (idempotent).
-- Database: onlinebookstore
-- Phiên bản giả định: MySQL 8.x (>= 8.0.16)
-- ============================================================

USE onlinebookstore;

-- 1. Xóa các bảng mới theo thứ tự phụ thuộc khóa ngoại
DROP TABLE IF EXISTS used_book_images;
DROP TABLE IF EXISTS coin_transactions;
DROP TABLE IF EXISTS admin_notifications;
DROP TABLE IF EXISTS used_books;

-- 2. Xóa các cột và CHECK constraint đã thêm vào orders và users
DROP PROCEDURE IF EXISTS rollback_consignment_and_book_coins;
DELIMITER //

CREATE PROCEDURE rollback_consignment_and_book_coins()
BEGIN
    -- Xóa cột orders.coins_used nếu tồn tại
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'orders'
          AND column_name = 'coins_used'
    ) THEN
        ALTER TABLE orders
            DROP COLUMN coins_used;
    END IF;

    -- Xóa CHECK constraint trên users.book_coins trước khi xóa cột
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND constraint_name = 'chk_users_book_coins'
          AND constraint_type = 'CHECK'
    ) THEN
        ALTER TABLE users
            DROP CHECK chk_users_book_coins;
    END IF;

    -- Xóa cột users.book_coins nếu tồn tại
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND column_name = 'book_coins'
    ) THEN
        ALTER TABLE users
            DROP COLUMN book_coins;
    END IF;
END //

DELIMITER ;
CALL rollback_consignment_and_book_coins();
DROP PROCEDURE IF EXISTS rollback_consignment_and_book_coins;
