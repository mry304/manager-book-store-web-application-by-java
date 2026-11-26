package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.bittercode.service.OrderService;
import com.bittercode.service.impl.OrderServiceImpl;
import com.bittercode.util.GHNClient;
import com.bittercode.util.GHNConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Webhook receiver for GHN order status callbacks.
 */
public class GHNWebhookServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(GHNWebhookServlet.class.getName());

    private static final long serialVersionUID = 1L;

    private final OrderService orderService = new OrderServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json; charset=UTF-8");
        res.getWriter().write("{\"error\":0,\"message\":\"GHN Webhook Endpoint is Active\"}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json; charset=UTF-8");

        String configuredSecret = GHNConfig.getWebhookSecret();
        if (configuredSecret.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            res.getWriter().write("{\"error\":1,\"message\":\"GHN webhook secret is not configured\"}");
            return;
        }

        String tokenHeader = req.getHeader("X-Webhook-Token");
        if (tokenHeader == null || !MessageDigest.isEqual(
                configuredSecret.getBytes(StandardCharsets.UTF_8),
                tokenHeader.getBytes(StandardCharsets.UTF_8))) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":1,\"message\":\"Unauthorized webhook request\"}");
            return;
        }

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        String payload = body.toString();
        if (payload.trim().isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"error\":1,\"message\":\"Empty request body\"}");
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String orderCode = firstNonEmpty(root.path("OrderCode").asText(""), root.path("order_code").asText(""));
            String status = firstNonEmpty(root.path("Status").asText(""), root.path("status").asText(""));
            String eventTime = firstNonEmpty(root.path("Time").asText(""), root.path("time").asText(""));
            int shopId = root.path("ShopID").asInt(root.path("shop_id").asInt(0));

            if (shopId > 0 && GHNConfig.getShopId() > 0 && shopId != GHNConfig.getShopId()) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.getWriter().write("{\"error\":1,\"message\":\"ShopID mismatch\"}");
                return;
            }

            if (orderCode.isEmpty() || status.isEmpty()) {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write("{\"error\":0,\"message\":\"Ignored webhook without OrderCode/Status\"}");
                return;
            }

            // Cross-verify critical terminal statuses against GHN API
            String lowerStatus = status.toLowerCase();
            if (("delivered".equals(lowerStatus) || "returned".equals(lowerStatus) || "cancel".equals(lowerStatus))
                    && GHNConfig.isConfigured()) {
                try {
                    JsonNode detail = GHNClient.getOrderDetail(orderCode);
                    String verifiedStatus = detail.path("status").asText("");
                    if (!verifiedStatus.isEmpty()) {
                        status = verifiedStatus;
                    }
                } catch (Exception verifyEx) {
                    LOGGER.warning("[GHNWebhook] Cross-check warning for " + orderCode + ": " + verifyEx.getMessage());
                }
            }

            orderService.updateOrderFromGHNWebhook(orderCode, status, eventTime, payload);
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\"error\":0,\"message\":\"Webhook processed\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Internal error";
            res.getWriter().write("{\"error\":1,\"message\":\"" + msg + "\"}");
        }
    }

    private String firstNonEmpty(String a, String b) {
        return a != null && !a.trim().isEmpty() ? a.trim() : (b != null ? b.trim() : "");
    }
}
