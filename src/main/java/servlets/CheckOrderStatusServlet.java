package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.bittercode.model.Order;
import com.bittercode.model.OrderStatus;
import com.bittercode.model.UserRole;
import com.bittercode.service.OrderService;
import com.bittercode.service.impl.OrderServiceImpl;
import com.bittercode.util.StoreUtil;

public class CheckOrderStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final OrderService orderService = new OrderServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json; charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        PrintWriter pw = res.getWriter();

        HttpSession session = req.getSession(false);
        boolean isCustomer = StoreUtil.isLoggedIn(UserRole.CUSTOMER, session);
        boolean isSeller = StoreUtil.isLoggedIn(UserRole.SELLER, session);
        if (!isCustomer && !isSeller) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            pw.write("{\"error\":1,\"message\":\"Vui lòng đăng nhập để kiểm tra đơn hàng\",\"isPaid\":false}");
            return;
        }

        String orderCodeParam = req.getParameter("orderCode");
        String orderIdParam = req.getParameter("orderId");

        Order order = null;
        try {
            if (orderCodeParam != null && !orderCodeParam.trim().isEmpty()) {
                long orderCode = Long.parseLong(orderCodeParam.trim());
                order = orderService.getOrderByOrderCode(orderCode);
            } else if (orderIdParam != null && !orderIdParam.trim().isEmpty()) {
                order = orderService.getOrderById(orderIdParam.trim());
            }

            if (order == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                pw.write("{\"error\":1,\"message\":\"Đơn hàng không tồn tại\",\"isPaid\":false}");
                return;
            }

            if (isCustomer && !isSeller) {
                String currentUser = StoreUtil.getCustomerUsername(session);
                if (currentUser == null || order.getUsername() == null
                        || !currentUser.equalsIgnoreCase(order.getUsername())) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    pw.write("{\"error\":1,\"message\":\"Bạn không có quyền xem đơn hàng này\",\"isPaid\":false}");
                    return;
                }
            }

            String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
            boolean isPaid = OrderStatus.CONFIRMED.name().equals(status) ||
                             OrderStatus.SHIPPING.name().equals(status) ||
                             OrderStatus.COMPLETED.name().equals(status);

            String json = String.format(
                    "{\"error\":0,\"orderCode\":%s,\"orderId\":\"%s\",\"status\":\"%s\",\"isPaid\":%b,\"totalAmount\":%.2f}",
                    order.getOrderCode() != null ? order.getOrderCode() : 0,
                    escape(order.getOrderId()),
                    escape(status),
                    isPaid,
                    order.getTotalAmount()
            );

            pw.write(json);

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            pw.write("{\"error\":1,\"message\":\"" + escape(e.getMessage()) + "\",\"isPaid\":false}");
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
