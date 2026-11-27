package com.bittercode.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bittercode.constant.BookCoinPolicy;
import com.bittercode.model.Book;
import com.bittercode.model.ConsignmentStatus;
import com.bittercode.model.StoreException;
import com.bittercode.model.UsedBook;
import com.bittercode.service.impl.BookServiceImpl;
import com.bittercode.service.impl.ConsignmentServiceImpl;
import com.bittercode.util.DBUtil;

class ConsignmentServiceTest {

    private ConsignmentService consignmentService;
    private BookService bookService;
    private ConsignmentTestHelper helper;

    @BeforeEach
    void setUp() {
        consignmentService = new ConsignmentServiceImpl();
        bookService = new BookServiceImpl();
        helper = new ConsignmentTestHelper();
    }

    @AfterEach
    void tearDown() {
        helper.cleanup();
    }

    @Test
    void testSubmitApproveCreatesSingleBookWithQuantityOne() throws Exception {
        String user = helper.createTestUser("seller_u", 0L);
        long id = consignmentService.submit(
                user,
                "T_Dế Mèn Phiêu Lưu Ký",
                "Tô Hoài",
                "NXB Kim Đồng",
                "Sách cũ còn rất mới",
                90_000,
                55_000,
                85,
                Arrays.asList("T_img1.jpg", "T_img2.jpg")
        );
        helper.trackUsedBookId(id);

        assertTrue(consignmentService.approve(id, "T_admin"));

        UsedBook approved = consignmentService.getById(id);
        assertNotNull(approved);
        assertEquals(ConsignmentStatus.LISTED, approved.getConsignmentStatus());
        String expectedBarcode = BookCoinPolicy.CONSIGNMENT_BARCODE_PREFIX + id;
        assertEquals(expectedBarcode, approved.getBookBarcode());

        Book listedBook = bookService.getBookById(expectedBarcode);
        assertNotNull(listedBook);
        assertEquals(1, listedBook.getQuantity());
        assertEquals(55_000.0, listedBook.getPrice());
        assertEquals(BookCoinPolicy.CONSIGNMENT_IMAGE_URL_PREFIX + "T_img1.jpg", listedBook.getImageUrl());
        assertEquals(1, countAdminNotificationsForRef(String.valueOf(id)));
    }

    @Test
    void testApproveWithLowConditionAndRejectWithShortReasonAreRejected() throws Exception {
        String user = helper.createTestUser("rule_u", 0L);
        long id = consignmentService.submit(
                user,
                "T_Sách Cũ 60%",
                "Tác giả Test",
                null,
                null,
                100_000,
                40_000,
                60,
                Collections.singletonList("T_low.jpg")
        );
        helper.trackUsedBookId(id);

        StoreException approveEx = assertThrows(StoreException.class,
                () -> consignmentService.approve(id, "T_admin"));
        assertEquals(BookCoinPolicy.ERR_APPROVAL_RULE, approveEx.getErrorCode());

        StoreException rejectEx = assertThrows(StoreException.class,
                () -> consignmentService.reject(id, "T_admin", "Ngắn"));
        assertEquals(BookCoinPolicy.ERR_VALIDATION, rejectEx.getErrorCode());
    }

    @Test
    void testRequestMoreInfoDeadlineAndExpireOverdueRequests() throws Exception {
        String user = helper.createTestUser("info_u", 0L);
        String otherUser = helper.createTestUser("other_u", 0L);
        long id = consignmentService.submit(
                user,
                "T_Sách Bổ Sung",
                "Tác giả B",
                null,
                null,
                120_000,
                70_000,
                80,
                Collections.singletonList("T_info1.jpg")
        );
        helper.trackUsedBookId(id);

        assertNull(consignmentService.getByIdForOwner(id, otherUser));
        assertNotNull(consignmentService.getByIdForOwner(id, user));

        assertTrue(consignmentService.requestMoreInfo(id, "T_admin", "Vui lòng chụp rõ trang bìa sau của sách"));
        UsedBook waitingInfo = consignmentService.getById(id);
        assertEquals(ConsignmentStatus.NEED_MORE_INFO, waitingInfo.getConsignmentStatus());
        assertNotNull(waitingInfo.getReviewedAt());
        assertNotNull(waitingInfo.getInfoDeadline());

        long diffMillis = waitingInfo.getInfoDeadline().getTime() - waitingInfo.getReviewedAt().getTime();
        long expectedMillis = TimeUnit.DAYS.toMillis(BookCoinPolicy.INFO_RESPONSE_DEADLINE_DAYS);
        assertTrue(Math.abs(diffMillis - expectedMillis) <= TimeUnit.MINUTES.toMillis(1),
                "info_deadline phải xấp xỉ +7 ngày kể từ lúc yêu cầu bổ sung");

        // Đặt info_deadline về quá khứ để kiểm tra expireOverdueInfoRequests()
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE used_books SET info_deadline = DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 HOUR) WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }

        int expired = consignmentService.expireOverdueInfoRequests();
        assertTrue(expired >= 1);

        UsedBook rejected = consignmentService.getById(id);
        assertEquals(ConsignmentStatus.REJECTED, rejected.getConsignmentStatus());
    }

    @Test
    void testMarkSoldForOrderThenRelistForOrder() throws Exception {
        String consignor = helper.createTestUser("consign_sold", 0L);
        String buyer = helper.createTestUser("buyer_sold", 0L);
        long usedBookId = consignmentService.submit(
                consignor,
                "T_Sách Bán Thử",
                "Tác giả C",
                null,
                null,
                100_000,
                60_000,
                90,
                Collections.singletonList("T_sold.jpg")
        );
        helper.trackUsedBookId(usedBookId);
        assertTrue(consignmentService.approve(usedBookId, "T_admin"));

        String barcode = BookCoinPolicy.CONSIGNMENT_BARCODE_PREFIX + usedBookId;
        String orderId = helper.createTestOrder(buyer, 60_000.0, 0, "CONFIRMED");
        helper.addOrderDetail(orderId, barcode, 1, 60_000.0);

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            assertEquals(1, consignmentService.markSoldForOrder(con, orderId));
            con.commit();
        }

        UsedBook soldBook = consignmentService.getById(usedBookId);
        assertEquals(ConsignmentStatus.SOLD, soldBook.getConsignmentStatus());
        assertEquals(orderId, soldBook.getSoldOrderId());
        assertNotNull(soldBook.getSoldAt());

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            assertEquals(1, consignmentService.relistForOrder(con, orderId));
            con.commit();
        }

        UsedBook relistedBook = consignmentService.getById(usedBookId);
        assertEquals(ConsignmentStatus.LISTED, relistedBook.getConsignmentStatus());
        assertNull(relistedBook.getSoldOrderId());
        assertNull(relistedBook.getSoldAt());

        List<UsedBook> ownerList = consignmentService.listByConsignor(consignor);
        assertEquals(1, ownerList.size());
    }

    @Test
    void testRespondToInfoRequestReturnsToPendingReviewAndNotifiesAdmin() throws Exception {
        String user = helper.createTestUser("resp_u", 0L);
        long id = consignmentService.submit(
                user,
                "T_Sách Chờ Sửa",
                "Tác giả D",
                null,
                null,
                100_000,
                50_000,
                75,
                Collections.singletonList("T_old.jpg")
        );
        helper.trackUsedBookId(id);

        assertTrue(consignmentService.requestMoreInfo(id, "T_admin", "Cập nhật thêm ảnh rõ nét hơn"));
        assertTrue(consignmentService.respondToInfoRequest(
                id,
                user,
                "T_Sách Đã Cập Nhật",
                "Tác giả D",
                "NXB Trẻ",
                "Đã bổ sung ảnh",
                100_000,
                50_000,
                80,
                Arrays.asList("T_new1.jpg", "T_new2.jpg")
        ));

        UsedBook updated = consignmentService.getById(id);
        assertEquals(ConsignmentStatus.PENDING_REVIEW, updated.getConsignmentStatus());
        assertEquals("T_Sách Đã Cập Nhật", updated.getTitle());
        assertNull(updated.getInfoDeadline());
        assertEquals(2, updated.getImageFileNames().size());
        assertEquals(2, countAdminNotificationsForRef(String.valueOf(id)));
    }

    private int countAdminNotificationsForRef(String refId) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM admin_notifications WHERE ref_id = ?")) {
            ps.setString(1, refId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
