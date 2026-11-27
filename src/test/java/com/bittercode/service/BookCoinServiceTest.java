package com.bittercode.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bittercode.model.CoinTransaction;
import com.bittercode.model.CoinTransactionType;
import com.bittercode.model.ConsignmentStatus;
import com.bittercode.service.impl.BookCoinServiceImpl;
import com.bittercode.util.DBUtil;

class BookCoinServiceTest {

    private BookCoinService bookCoinService;
    private ConsignmentTestHelper helper;

    @BeforeEach
    void setUp() {
        bookCoinService = new BookCoinServiceImpl();
        helper = new ConsignmentTestHelper();
    }

    @AfterEach
    void tearDown() {
        helper.cleanup();
    }

    @Test
    void testCalculateMaxRedeemablePolicyCases() {
        assertEquals(50_000L, bookCoinService.calculateMaxRedeemable(1_000_000L, 100_000L));
        assertEquals(500L, bookCoinService.calculateMaxRedeemable(1_000_000L, 1_500L));
        assertEquals(0L, bookCoinService.calculateMaxRedeemable(1_000_000L, 1_000L));
        assertEquals(200L, bookCoinService.calculateMaxRedeemable(200L, 100_000L));
    }

    @Test
    void testRewardConsignorTwiceIsIdempotent() throws Exception {
        String user = helper.createTestUser("consignor", 10_000L);
        String orderId = helper.createTestOrder(user, 100_000.0, 0, "COMPLETED");
        long usedBookId = helper.insertDirectUsedBook(user, 150_000, 80_000, 85,
                ConsignmentStatus.SOLD.name(), null, orderId);

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            assertTrue(bookCoinService.rewardConsignor(con, usedBookId, orderId));
            assertFalse(bookCoinService.rewardConsignor(con, usedBookId, orderId));
            con.commit();
        }

        assertEquals(18_000L, bookCoinService.getBalance(user));
        assertEquals(1, helper.countLedgerRows(CoinTransactionType.CONSIGN_REWARD.name(), String.valueOf(usedBookId)));
    }

    @Test
    void testRedeemForOrderInsufficientBalanceReturnsFalse() throws Exception {
        String user = helper.createTestUser("buyer_low", 5_000L);
        String orderId = helper.createTestOrder(user, 100_000.0, 0, "PENDING");

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            boolean ok = bookCoinService.redeemForOrder(con, user, orderId, 10_000L);
            assertFalse(ok);
            con.commit();
        }

        assertEquals(5_000L, bookCoinService.getBalance(user));
        assertEquals(0, helper.countLedgerRows(CoinTransactionType.ORDER_REDEEM.name(), orderId));
    }

    @Test
    void testRedeemThenRefundTwiceRestoresExactBalance() throws Exception {
        String user = helper.createTestUser("buyer_refund", 50_000L);
        String orderId = helper.createTestOrder(user, 100_000.0, 20_000, "PENDING");

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            assertTrue(bookCoinService.redeemForOrder(con, user, orderId, 20_000L));
            assertTrue(bookCoinService.refundForOrder(con, orderId));
            assertFalse(bookCoinService.refundForOrder(con, orderId));
            con.commit();
        }

        assertEquals(50_000L, bookCoinService.getBalance(user));
        assertEquals(1, helper.countLedgerRows(CoinTransactionType.ORDER_REDEEM.name(), orderId));
        assertEquals(1, helper.countLedgerRows(CoinTransactionType.ORDER_REFUND.name(), orderId));

        List<CoinTransaction> history = bookCoinService.getHistory(user, 10, 0);
        assertEquals(2, history.size());
    }

    @Test
    void testRewardConsignorConcurrentThreadsOnlyOneLedgerRow() throws Exception {
        String user = helper.createTestUser("concurrent_u", 0L);
        String orderId = helper.createTestOrder(user, 120_000.0, 0, "COMPLETED");
        long usedBookId = helper.insertDirectUsedBook(user, 200_000, 120_000, 90,
                ConsignmentStatus.SOLD.name(), null, orderId);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> f1 = pool.submit(() -> runConcurrentReward(usedBookId, orderId, ready, start));
            Future<Boolean> f2 = pool.submit(() -> runConcurrentReward(usedBookId, orderId, ready, start));

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            boolean r1 = f1.get(15, TimeUnit.SECONDS);
            boolean r2 = f2.get(15, TimeUnit.SECONDS);
            assertTrue(r1 ^ r2, "Đúng 1 luồng phải thưởng thành công và 1 luồng bị chặn trùng");
        } finally {
            pool.shutdownNow();
        }

        assertEquals(12_000L, bookCoinService.getBalance(user));
        assertEquals(1, helper.countLedgerRows(CoinTransactionType.CONSIGN_REWARD.name(), String.valueOf(usedBookId)));
    }

    private boolean runConcurrentReward(long usedBookId, String orderId,
                                        CountDownLatch ready, CountDownLatch start) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) {
                con.rollback();
                return false;
            }
            boolean rewarded = bookCoinService.rewardConsignor(con, usedBookId, orderId);
            con.commit();
            return rewarded;
        }
    }
}
