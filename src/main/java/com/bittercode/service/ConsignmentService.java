package com.bittercode.service;

import java.sql.Connection;
import java.util.List;

import com.bittercode.model.StoreException;
import com.bittercode.model.UsedBook;

/**
 * Service quản lý quy trình ký gửi sách cũ.
 */
public interface ConsignmentService {

    /** Khách gửi yêu cầu ký gửi mới (tạo used_books, used_book_images và admin_notifications trong 1 transaction). */
    long submit(String username, String title, String author, String publisher, String description,
                int originalPrice, int resalePrice, int conditionPercent, List<String> imageFileNames)
            throws StoreException;

    /** Lấy danh sách yêu cầu ký gửi của một khách hàng. */
    List<UsedBook> listByConsignor(String username) throws StoreException;

    /** Lấy chi tiết yêu cầu ký gửi nếu đúng chủ sở hữu; trả null nếu không tồn tại hoặc không phải chủ. */
    UsedBook getByIdForOwner(long id, String username) throws StoreException;

    /** Lấy danh sách yêu cầu ký gửi cho Admin (hỗ trợ lọc trạng thái và phân trang). */
    List<UsedBook> listForAdmin(String statusFilter, int limit, int offset) throws StoreException;

    /** Lấy chi tiết yêu cầu ký gửi theo id. */
    UsedBook getById(long id) throws StoreException;

    /** Admin duyệt ký gửi: tạo sách mới trong bảng books (quantity=1) và chuyển trạng thái LISTED. */
    boolean approve(long id, String adminUsername) throws StoreException;

    /** Admin từ chối ký gửi với lý do tối thiểu 10 ký tự. */
    boolean reject(long id, String adminUsername, String reason) throws StoreException;

    /** Admin yêu cầu bổ sung thông tin (ghi chú >= 10 ký tự, đặt hạn bổ sung +7 ngày). */
    boolean requestMoreInfo(long id, String adminUsername, String note) throws StoreException;

    /** Khách hàng phản hồi yêu cầu bổ sung thông tin khi chưa quá hạn, đưa về PENDING_REVIEW và báo Admin. */
    boolean respondToInfoRequest(long id, String username, String title, String author, String publisher,
                                 String description, int originalPrice, int resalePrice, int conditionPercent,
                                 List<String> newImageFileNames) throws StoreException;

    /** Đánh dấu REJECTED cho các yêu cầu NEED_MORE_INFO đã quá hạn bổ sung; có thể chạy lại an toàn. */
    int expireOverdueInfoRequests() throws StoreException;

    /** Đánh dấu SOLD cho các sách ký gửi đang LISTED thuộc đơn hàng (tham gia transaction của caller). */
    int markSoldForOrder(Connection con, String orderId) throws StoreException;

    /** Đưa sách ký gửi SOLD của đơn hàng trở lại LISTED khi đơn bị hủy sau xác nhận (tham gia transaction của caller). */
    int relistForOrder(Connection con, String orderId) throws StoreException;
}
