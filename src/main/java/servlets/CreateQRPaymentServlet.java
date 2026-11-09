package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.bittercode.model.Cart;
import com.bittercode.model.UserRole;
import com.bittercode.service.OrderService;
import com.bittercode.service.impl.OrderServiceImpl;
import com.bittercode.util.PayOSConfig;
import com.bittercode.util.StoreUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet tạo payment link VietQR qua PayOS REST API (direct HTTP call).
 * Không dùng PayOS SDK để tránh lỗi signature verification.
 */
public class CreateQRPaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String PAYOS_API_URL = "https://api-merchant.payos.vn/v2/payment-requests";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OrderService orderService = new OrderServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.sendRedirect(req.getContextPath() + "/checkout");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=UTF-8");

        HttpSession session = req.getSession();
        if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)) {
            res.sendRedirect(req.getContextPath() + "/userlog");
            return;
        }

        String username = (String) session.getAttribute(UserRole.CUSTOMER.toString());
        if (username == null) username = "customer";

        List<Cart> cartItems = (List<Cart>) session.getAttribute("cartItems");
        if (cartItems == null || cartItems.isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        // 1. Đọc thông tin giao hàng
        String fullName    = trim(req.getParameter("firstname"));
        String email       = trim(req.getParameter("email"));
        String address     = trim(req.getParameter("address"));
        String city        = trim(req.getParameter("city"));
        String state       = trim(req.getParameter("state"));
        String zip         = trim(req.getParameter("zip"));

        if (fullName.length() < 2 || email.isEmpty() || address.length() < 5) {
            res.sendRedirect(req.getContextPath() + "/checkout?error=" +
                    URLEncoder.encode("Vui lòng điền đầy đủ họ tên, email và địa chỉ nhận hàng.", StandardCharsets.UTF_8));
            return;
        }

        String shippingAddress = address;
        if (!state.isEmpty()) shippingAddress += ", " + state;
        if (!city.isEmpty())  shippingAddress += ", " + city;
        if (!zip.isEmpty())   shippingAddress += " (Mã bưu điện: " + zip + ")";

        // 2. Tính tổng tiền
        double totalAmount = 0.0;
        for (Cart c : cartItems) {
            totalAmount += c.getBook().getPrice() * c.getQuantity();
        }
        // PayOS yêu cầu tối thiểu 1.000 VND
        int amountInt = Math.max(1000, (int) Math.round(totalAmount));

        // 3. Tạo orderCode duy nhất (số nguyên dương, không vượt quá 9007199254740991)
        long orderCode = (System.currentTimeMillis() / 1000L) * 1000L + (long)(Math.random() * 999);

        // 4. Tạo description (tối đa 25 ký tự, chỉ chữ và số)
        String description = "DH" + orderCode;
        if (description.length() > 25) description = description.substring(0, 25);

        try {
            // 5. Lưu đơn hàng vào DB với trạng thái PENDING
            String orderId = orderService.createOrderWithCode(username, cartItems, totalAmount,
                    email, shippingAddress, "Mã VietQR (PayOS)", orderCode);

            // Xóa giỏ hàng sau khi đã đặt
            StoreUtil.clearUserCart(session, username);

            String returnUrl = PayOSConfig.getReturnUrl() + "?orderCode=" + orderCode;
            String cancelUrl = PayOSConfig.getCancelUrl() + "?orderCode=" + orderCode + "&cancel=true";

            String qrCode       = null;
            String checkoutUrl  = null;
            String accountNumber = "";
            String accountName  = "";
            String bin          = "";

            // 6. Gọi PayOS REST API trực tiếp (không qua SDK để tránh lỗi checksum)
            if (PayOSConfig.isConfigured()) {
                try {
                    // Xây dựng JSON body
                    StringBuilder itemsJson = new StringBuilder("[");
                    for (int i = 0; i < cartItems.size(); i++) {
                        Cart c = cartItems.get(i);
                        if (i > 0) itemsJson.append(",");
                        itemsJson.append("{")
                                .append("\"name\":\"").append(escapeJson(c.getBook().getName())).append("\",")
                                .append("\"quantity\":").append(c.getQuantity()).append(",")
                                .append("\"price\":").append((int) Math.round(c.getBook().getPrice()))
                                .append("}");
                    }
                    itemsJson.append("]");

                    // Tính chữ ký HMAC-SHA256 (bắt buộc khi gọi API trực tiếp)
                    // PayOS ký theo thứ tự alphabet: amount, cancelUrl, description, orderCode, returnUrl
                    String signatureData = "amount=" + amountInt
                            + "&cancelUrl=" + cancelUrl
                            + "&description=" + description
                            + "&orderCode=" + orderCode
                            + "&returnUrl=" + returnUrl;
                    String signature = hmacSHA256(signatureData, PayOSConfig.getChecksumKey());
                    System.out.println("[PayOS-DIRECT] signatureData = " + signatureData);
                    System.out.println("[PayOS-DIRECT] signature     = " + signature);

                    String jsonBody = "{"
                            + "\"orderCode\":" + orderCode + ","
                            + "\"amount\":" + amountInt + ","
                            + "\"description\":\"" + escapeJson(description) + "\","
                            + "\"buyerName\":\"" + escapeJson(fullName) + "\","
                            + "\"buyerEmail\":\"" + escapeJson(email) + "\","
                            + "\"items\":" + itemsJson + ","
                            + "\"returnUrl\":\"" + escapeJson(returnUrl) + "\","
                            + "\"cancelUrl\":\"" + escapeJson(cancelUrl) + "\","
                            + "\"signature\":\"" + signature + "\""
                            + "}";

                    System.out.println("[PayOS-DIRECT] Request body: " + jsonBody);

                    // Gửi HTTP POST
                    URL url = new URL(PAYOS_API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("x-client-id", PayOSConfig.getClientId());
                    conn.setRequestProperty("x-api-key", PayOSConfig.getApiKey());
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(15000);
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    }

                    int httpCode = conn.getResponseCode();
                    InputStream is = (httpCode >= 200 && httpCode < 300)
                            ? conn.getInputStream() : conn.getErrorStream();
                    String rawResponse = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    System.out.println("[PayOS-DIRECT] HTTP Status: " + httpCode);
                    System.out.println("[PayOS-DIRECT] Response: " + rawResponse);

                    // Parse JSON response
                    JsonNode root = MAPPER.readTree(rawResponse);
                    String code = root.path("code").asText("");

                    if ("00".equals(code)) {
                        JsonNode data = root.path("data");
                        checkoutUrl   = data.path("checkoutUrl").asText(null);
                        qrCode        = data.path("qrCode").asText(null);
                        accountNumber = data.path("accountNumber").asText("");
                        accountName   = data.path("accountName").asText("");
                        bin           = data.path("bin").asText("");

                        System.out.println("[PayOS-DIRECT] ✅ SUCCESS!");
                        System.out.println("[PayOS-DIRECT] accountNumber = " + accountNumber);
                        System.out.println("[PayOS-DIRECT] accountName   = " + accountName);
                        System.out.println("[PayOS-DIRECT] bin           = " + bin);
                        System.out.println("[PayOS-DIRECT] checkoutUrl   = " + checkoutUrl);
                    } else {
                        String desc2 = root.path("desc").asText("unknown");
                        System.err.println("[PayOS-DIRECT] ❌ API Error code=" + code + " desc=" + desc2);
                        System.err.println("[PayOS-DIRECT] Full response: " + rawResponse);
                    }

                    conn.disconnect();
                } catch (Exception apiEx) {
                    System.err.println("[PayOS-DIRECT] Exception calling PayOS API: " + apiEx.getMessage());
                    apiEx.printStackTrace();
                }
            }

            // 7. Fallback nếu PayOS thất bại
            if (qrCode == null || qrCode.trim().isEmpty()) {
                System.err.println("[PayOS-DIRECT] FALLBACK activated — dùng VietQR tĩnh.");
                String defaultBankBin   = "970422"; // MB Bank
                String defaultAccountNo = "0888999888";
                String defaultAccName   = "CONG TY SACH BOOKS";
                String encodedDesc = URLEncoder.encode(description, StandardCharsets.UTF_8);
                qrCode = "https://img.vietqr.io/image/" + defaultBankBin + "-" + defaultAccountNo
                        + "-compact2.png?amount=" + amountInt
                        + "&addInfo=" + encodedDesc
                        + "&accountName=" + URLEncoder.encode(defaultAccName, StandardCharsets.UTF_8);
                accountNumber = defaultAccountNo;
                accountName   = defaultAccName;
                bin           = defaultBankBin;
            }

            req.setAttribute("orderCode",       orderCode);
            req.setAttribute("orderId",         orderId);
            req.setAttribute("totalAmount",     totalAmount);
            req.setAttribute("qrCode",          qrCode);
            req.setAttribute("checkoutUrl",     checkoutUrl);
            req.setAttribute("accountNumber",   accountNumber);
            req.setAttribute("accountName",     accountName);
            req.setAttribute("bin",             bin);
            req.setAttribute("description",     description);
            req.setAttribute("shippingEmail",   email);
            req.setAttribute("shippingAddress", shippingAddress);

            RequestDispatcher rd = req.getRequestDispatcher("payment-checkout.jsp");
            rd.forward(req, res);

        } catch (Exception e) {
            e.printStackTrace();
            res.sendRedirect(req.getContextPath() + "/checkout?error=" +
                    URLEncoder.encode("Có lỗi xảy ra khi tạo giao dịch: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /** Escape ký tự đặc biệt trong JSON string */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Tính HMAC-SHA256 signature theo yêu cầu của PayOS REST API.
     * PayOS dùng chữ ký này để xác thực rằng request đến từ merchant hợp lệ.
     */
    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }
        return hex.toString();
    }
}
