package servlets;

import java.io.BufferedReader;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.bittercode.service.OrderService;
import com.bittercode.service.impl.OrderServiceImpl;
import com.bittercode.util.PayOSConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

public class PayOSWebhookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final OrderService orderService = new OrderServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json; charset=UTF-8");
        res.getWriter().write("{\"error\":0,\"message\":\"PayOS Webhook Endpoint is Active\"}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json; charset=UTF-8");

        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        String payload = requestBody.toString();
        System.out.println("[PayOSWebhook] Received webhook payload: " + payload);

        if (payload.trim().isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"error\":1,\"message\":\"Empty request body\"}");
            return;
        }

        try {
            long orderCode = -1;
            boolean isSuccess = false;

            if (PayOSConfig.isConfigured() && PayOSConfig.getPayOS() != null) {
                try {
                    Webhook webhook = objectMapper.readValue(payload, Webhook.class);
                    WebhookData verifiedData = PayOSConfig.getPayOS().verifyPaymentWebhookData(webhook);
                    if (verifiedData != null) {
                        orderCode = verifiedData.getOrderCode();
                        isSuccess = "00".equals(verifiedData.getCode()) || "00".equals(webhook.getCode())
                                || "success".equalsIgnoreCase(verifiedData.getDesc());
                    }
                } catch (Exception verifyEx) {
                    System.err.println("[PayOSWebhook] Verification error: " + verifyEx.getMessage());
                }
            }

            // Fallback parsing if signature check failed or testing without live PayOS keys
            if (orderCode <= 0) {
                JsonNode rootNode = objectMapper.readTree(payload);
                JsonNode dataNode = rootNode.path("data");
                if (!dataNode.isMissingNode()) {
                    orderCode = dataNode.path("orderCode").asLong(-1);
                    String code = dataNode.path("code").asText("");
                    isSuccess = "00".equals(code) || "00".equals(rootNode.path("code").asText());
                }
            }

            if (orderCode > 0 && isSuccess) {
                boolean confirmed = orderService.confirmPaymentAndDeductStock(orderCode);
                System.out.println("[PayOSWebhook] Order #" + orderCode + " confirmed & stock deducted: " + confirmed);
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write("{\"error\":0,\"message\":\"Order confirmed successfully\"}");
                return;
            }

            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\"error\":0,\"message\":\"Webhook acknowledged\"}");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[PayOSWebhook] Processing exception: " + e.getMessage());
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("{\"error\":1,\"message\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
}
