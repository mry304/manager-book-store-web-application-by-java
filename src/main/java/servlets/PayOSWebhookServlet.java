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
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.logging.Level;
import java.util.logging.Logger;
public class PayOSWebhookServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PayOSWebhookServlet.class.getName());

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

        if (payload.trim().isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"error\":1,\"message\":\"Empty request body\"}");
            return;
        }

        if (!PayOSConfig.isConfigured() || PayOSConfig.getPayOS() == null) {
            res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            res.getWriter().write("{\"error\":1,\"message\":\"PayOS webhook verification is not configured\"}");
            return;
        }

        try {
            Webhook webhook = objectMapper.readValue(payload, Webhook.class);
            WebhookData verifiedData;
            try {
                verifiedData = PayOSConfig.getPayOS().verifyPaymentWebhookData(webhook);
            } catch (Exception verifyEx) {
                LOGGER.warning("[PayOSWebhook] Signature verification failed: " + verifyEx.getMessage());
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\":1,\"message\":\"Invalid webhook signature\"}");
                return;
            }

            if (verifiedData == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\":1,\"message\":\"Invalid webhook signature\"}");
                return;
            }

            long orderCode = verifiedData.getOrderCode();
            boolean isSuccess = "00".equals(verifiedData.getCode()) || "00".equals(webhook.getCode())
                    || "success".equalsIgnoreCase(verifiedData.getDesc());

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
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            LOGGER.warning("[PayOSWebhook] Processing exception: " + e.getMessage());
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("{\"error\":1,\"message\":\"Internal server error\"}");
        }
    }
}
