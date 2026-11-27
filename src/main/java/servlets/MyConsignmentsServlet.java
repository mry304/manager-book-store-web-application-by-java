package servlets;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.bittercode.model.UsedBook;
import com.bittercode.service.ConsignmentService;
import com.bittercode.service.impl.ConsignmentServiceImpl;

/**
 * Servlet hiển thị danh sách yêu cầu ký gửi sách cũ của khách hàng đang đăng nhập.
 * URL: /my-consignments
 */
public class MyConsignmentsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(MyConsignmentsServlet.class.getName());
    private static final long serialVersionUID = 1L;

    private final ConsignmentService consignmentService = new ConsignmentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=UTF-8");

        String username = ConsignmentServletHelper.requireCustomer(req, res);
        if (username == null) {
            return;
        }

        HttpSession session = req.getSession();
        String flashMsg = (String) session.getAttribute("consignFlash");
        if (flashMsg != null) {
            req.setAttribute("flashMsg", flashMsg);
            session.removeAttribute("consignFlash");
        }

        List<UsedBook> consignments;
        try {
            consignments = consignmentService.listByConsignor(username);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading customer consignments", e);
            consignments = Collections.emptyList();
            req.setAttribute("errorMsg", "Không thể tải danh sách ký gửi lúc này, vui lòng thử lại sau.");
        }

        req.setAttribute("consignments", consignments);
        req.getRequestDispatcher("/my-consignments.jsp").forward(req, res);
    }
}
