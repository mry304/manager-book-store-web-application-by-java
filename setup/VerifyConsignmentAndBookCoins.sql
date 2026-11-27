-- ============================================================
-- Verify: VerifyConsignmentAndBookCoins.sql
-- Mục đích: Kiểm tra cấu trúc 4 bảng mới, 2 bảng đã ALTER
--           và chứng minh ràng buộc UNIQUE KEY uq_coin_type_ref
--           chặn ghi trùng (type, ref_id) mà không để lại dữ liệu rác.
-- Database: onlinebookstore
-- Phiên bản giả định: MySQL 8.x (>= 8.0.16)
-- Lưu ý khi chạy bằng CLI: dùng `mysql -u root -p --force < setup/VerifyConsignmentAndBookCoins.sql`
-- (hoặc chạy từng phần trong MySQL Workbench) để script tiếp tục tới lệnh ROLLBACK
-- sau khi câu INSERT thứ 2 bị từ chối với ERROR 1062.
-- ============================================================

USE onlinebookstore;

-- 1. Kiểm tra định nghĩa đầy đủ 4 bảng mới (cột, FK, index, charset/engine)
SHOW CREATE TABLE used_books;
SHOW CREATE TABLE used_book_images;
SHOW CREATE TABLE coin_transactions;
SHOW CREATE TABLE admin_notifications;

-- 2. Kiểm tra cấu trúc cột của users và orders sau ALTER
DESCRIBE users;
DESCRIBE orders;

-- 3. Kiểm tra CHECK constraint trên users.book_coins đã được đăng ký & thực thi
SELECT tc.constraint_name, tc.constraint_type, cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc
  ON tc.constraint_schema = cc.constraint_schema
 AND tc.constraint_name = cc.constraint_name
WHERE tc.table_schema = DATABASE()
  AND tc.table_name = 'users'
  AND tc.constraint_name = 'chk_users_book_coins';

-- 4. Bài test chống trùng lặp (type, ref_id) trong transaction + ROLLBACK
START TRANSACTION;

-- Tạo user tạm trong cùng transaction để thỏa mãn FK fk_coin_transactions_user
INSERT INTO users (username, password, firstname, lastname, address, phone, mailid, usertype)
VALUES ('__verify_coin_user__', 'verify_only', 'Verify', 'Test', 'Test Address', '0000000000', 'verify_coin@example.local', 2);

-- Dòng 1: PHẢI THÀNH CÔNG (1 row affected)
INSERT INTO coin_transactions (username, amount, type, ref_id, order_id, balance_after, note)
VALUES ('__verify_coin_user__', 50000, 'CONSIGN_REWARD', 'VERIFY_DUP_001', NULL, 50000, 'Verify insert 1 - expected OK');

-- Dòng 2 cùng (type, ref_id): PHẢI BỊ TỪ CHỐI với lỗi
-- ERROR 1062 (23000): Duplicate entry 'CONSIGN_REWARD-VERIFY_DUP_001' for key 'coin_transactions.uq_coin_type_ref'
INSERT INTO coin_transactions (username, amount, type, ref_id, order_id, balance_after, note)
VALUES ('__verify_coin_user__', 50000, 'CONSIGN_REWARD', 'VERIFY_DUP_001', NULL, 100000, 'Verify duplicate insert 2 - expected REJECT');

-- Hoàn tác toàn bộ dữ liệu test (kể cả khi session ngắt vì lỗi 1062, MySQL cũng tự rollback transaction chưa commit)
ROLLBACK;
