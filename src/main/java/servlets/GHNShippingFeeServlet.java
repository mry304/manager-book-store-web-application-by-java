package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.bittercode.model.Cart;
import com.bittercode.util.GHNClient;

/**
 * Server-side GHN shipping fee calculator for checkout preview.
 */
public class GHNShippingFeeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json; charset=UTF-8");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        try {
            int districtId = Integer.parseInt(req.getParameter("districtId").trim());
            String wardCode = req.getParameter("wardCode");
            if (wardCode == null || wardCode.trim().isEmpty()) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":1,\"message\":\"Thiếu mã phường/xã\"}");
                return;
            }

            int totalQty = 0;
            double totalAmount = 0;
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("cartItems") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Cart> cartItems = (List<Cart>) session.getAttribute("cartItems");
                for (Cart c : cartItems) {
                    totalQty += Math.max(1, c.getQuantity());
                    totalAmount += c.getBook().getPrice() * c.getQuantity();
                }
            }
            int weight = Math.max(500, totalQty * 500);
            int insuranceValue = (int) Math.round(totalAmount);

            GHNClient.ShippingFeeResult fee = GHNClient.calculateFee(districtId, wardCode.trim(), weight, insuranceValue);
            out.write(String.format("{\"error\":0,\"totalFee\":%d,\"serviceFee\":%d,\"insuranceFee\":%d,\"freeship\":true}",
                    fee.total, fee.serviceFee, fee.insuranceFee));
        } catch (NumberFormatException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":1,\"message\":\"Mã quận/huyện không hợp lệ\"}");
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_OK);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Không tính được phí GHN";
            out.write("{\"error\":1,\"totalFee\":0,\"freeship\":true,\"message\":\"" + msg + "\"}");
        }
    }
}
