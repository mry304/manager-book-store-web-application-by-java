package com.bittercode.service;

import java.sql.Connection;
import java.util.List;

import com.bittercode.model.CoinTransaction;
import com.bittercode.model.StoreException;

/**
 * Service quản lý số dư và sổ cái giao dịch Book Coins.
 * Các phương thức nhận Connection từ bên gọi sẽ tham gia giao dịch của bên gọi
 * (không tự commit, rollback hay đóng Connection).
 */
public interface BookCoinService {

    /** Lấy số dư Book Coins hiện tại của người dùng (tự mở kết nối). */
    long getBalance(String username) throws StoreException;

    /** Lấy lịch sử giao dịch Book Coins của người dùng theo trang (tự mở kết nối). */
    List<CoinTransaction> getHistory(String username, int limit, int offset) throws StoreException;

    /** Tính số Book Coins tối đa có thể dùng cho đơn hàng dựa trên chính sách hiện hành. */
    long calculateMaxRedeemable(long balance, long goodsTotal);

    /** Trừ Book Coins cho đơn hàng và ghi nhận giao dịch ORDER_REDEEM (tham gia transaction của caller). */
    boolean redeemForOrder(Connection con, String username, String orderId, long coins) throws StoreException;

    /** Hoàn Book Coins đã dùng của đơn hàng và ghi ORDER_REFUND (tham gia transaction của caller). */
    boolean refundForOrder(Connection con, String orderId) throws StoreException;

    /** Cộng thưởng Book Coins cho người ký gửi một cuốn sách cũ và ghi CONSIGN_REWARD (tham gia transaction của caller). */
    boolean rewardConsignor(Connection con, long usedBookId, String orderId) throws StoreException;

    /** Cộng thưởng cho tất cả sách ký gửi trạng thái SOLD thuộc đơn hàng đã hoàn thành. */
    int rewardForCompletedOrder(Connection con, String orderId) throws StoreException;
}
