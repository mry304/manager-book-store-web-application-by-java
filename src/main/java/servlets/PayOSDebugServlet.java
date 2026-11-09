package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.bittercode.util.PayOSConfig;

/**
 * DEBUG ONLY — Xóa servlet này trước khi deploy production.
 * Truy cập: http://localhost:8080/onlinebookstore/payos-debug
 */
public class PayOSDebugServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/plain; charset=UTF-8");
        PrintWriter out = res.getWriter();

        String action = req.getParameter("action");

        if ("reset".equals(action)) {
            PayOSConfig.resetInstance();
            out.println("✅ PayOS Singleton has been RESET.");
            out.println("Call this URL again without ?action=reset to re-initialize and verify.");
            return;
        }

        String clientId = PayOSConfig.getClientId();
        String apiKey = PayOSConfig.getApiKey();
        String checksumKey = PayOSConfig.getChecksumKey();
        boolean configured = PayOSConfig.isConfigured();

        out.println("=== PayOS Configuration Debug ===");
        out.println("");
        out.println("isConfigured : " + configured);
        out.println("clientId     : " + clientId);
        out.println("apiKey       : " + (apiKey.isEmpty() ? "(empty)" : apiKey.substring(0, Math.min(8, apiKey.length())) + "... (total " + apiKey.length() + " chars)"));
        out.println("checksumKey  : " + (checksumKey.isEmpty() ? "(empty)" : checksumKey.substring(0, Math.min(8, checksumKey.length())) + "... (total " + checksumKey.length() + " chars)"));
        out.println("returnUrl    : " + PayOSConfig.getReturnUrl());
        out.println("cancelUrl    : " + PayOSConfig.getCancelUrl());
        out.println("");

        if (configured) {
            // Try initializing PayOS and creating a minimal test
            try {
                vn.payos.PayOS payos = PayOSConfig.getPayOS();
                if (payos != null) {
                    out.println("✅ PayOS SDK instance: INITIALIZED");

                    // Try a real API call with minimal test data
                    try {
                        vn.payos.type.PaymentData testPayment = vn.payos.type.PaymentData.builder()
                                .orderCode(System.currentTimeMillis() / 1000)
                                .amount(2000)  // 2000 VND minimum safe amount
                                .description("TestDebug")
                                .returnUrl("https://example.com/success")
                                .cancelUrl("https://example.com/cancel")
                                .build();

                        out.println("⏳ Calling PayOS.createPaymentLink() with test data...");
                        vn.payos.type.CheckoutResponseData data = payos.createPaymentLink(testPayment);
                        out.println("✅ SUCCESS! PayOS API working correctly.");
                        out.println("   accountNumber : " + data.getAccountNumber());
                        out.println("   accountName   : " + data.getAccountName());
                        out.println("   bin           : " + data.getBin());
                        out.println("   checkoutUrl   : " + data.getCheckoutUrl());
                    } catch (Exception apiEx) {
                        out.println("❌ PayOS API call FAILED: " + apiEx.getClass().getSimpleName());
                        out.println("   Message: " + apiEx.getMessage());
                        out.println("");
                        out.println("   → Nếu lỗi là 'signature does not match':");
                        out.println("     checksumKey trong application.properties chưa đúng.");
                        out.println("     Kiểm tra lại tại: https://my.payos.vn → Thông tin tích hợp");
                        out.println("");
                        out.println("   → Để reset Singleton sau khi sửa key:");
                        out.println("     Truy cập URL: /payos-debug?action=reset");
                    }
                } else {
                    out.println("❌ PayOS SDK instance: NULL (initialization failed)");
                }
            } catch (Exception e) {
                out.println("❌ Exception: " + e.getMessage());
            }
        } else {
            out.println("❌ PayOS NOT configured. Check application.properties keys.");
        }

        out.println("");
        out.println("=== To reset PayOS singleton ===");
        out.println("Visit: " + req.getContextPath() + "/payos-debug?action=reset");
    }
}
