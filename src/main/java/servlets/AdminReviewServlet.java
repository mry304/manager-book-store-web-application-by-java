package servlets;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.bittercode.model.BookReview;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookReviewService;
import com.bittercode.service.impl.BookReviewServiceImpl;
import com.bittercode.util.StoreUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Servlet quản lý đánh giá dành cho Admin.
 * Mapping: /admin-reviews
 */
public class AdminReviewServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminReviewServlet.class.getName());


    private final BookReviewService reviewService = new BookReviewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();

        // Kiểm tra quyền Admin (usertype == 1 ~ SELLER role)
        if (!StoreUtil.isLoggedIn(UserRole.SELLER, session)) {
            res.sendRedirect("SellerLogin.html");
            return;
        }

        // Lấy tham số lọc
        String keyword = req.getParameter("keyword");
        String ratingFilter = req.getParameter("rating");
        String replyStatus = req.getParameter("replyStatus");

        List<BookReview> reviews;
        try {
            reviews = reviewService.getAllReviewsForAdmin(keyword, ratingFilter, replyStatus);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            reviews = java.util.Collections.emptyList();
        }

        req.setAttribute("reviews", reviews);
        req.setAttribute("keyword", keyword);
        req.setAttribute("ratingFilter", ratingFilter);
        req.setAttribute("replyStatus", replyStatus);

        // Flash message
        String flashMsg = (String) session.getAttribute("adminReviewFlash");
        if (flashMsg != null) {
            req.setAttribute("flashMsg", flashMsg);
            session.removeAttribute("adminReviewFlash");
        }

        req.getRequestDispatcher("/admin-reviews.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();

        // Kiểm tra quyền Admin
        if (!StoreUtil.isLoggedIn(UserRole.SELLER, session)) {
            res.sendRedirect("SellerLogin.html");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "";

        // Lấy admin username từ session
        Object sellerObj = session.getAttribute(UserRole.SELLER.toString());
        String adminUsername = sellerObj != null ? sellerObj.toString() : "admin";

        try {
            switch (action) {
                case "reply": {
                    int reviewId = Integer.parseInt(req.getParameter("reviewId"));
                    String replyContent = req.getParameter("replyContent");
                    if (replyContent != null && !replyContent.trim().isEmpty()) {
                        String result = reviewService.replyToReview(reviewId, replyContent.trim(), adminUsername);
                        if ("SUCCESS".equalsIgnoreCase(result)) {
                            session.setAttribute("adminReviewFlash", "Phản hồi đã được lưu thành công!");
                        } else {
                            session.setAttribute("adminReviewFlash", "Lưu phản hồi thất bại. Vui lòng thử lại.");
                        }
                    } else {
                        session.setAttribute("adminReviewFlash", "Nội dung phản hồi không được để trống.");
                    }
                    break;
                }
                case "toggle_status": {
                    int reviewId = Integer.parseInt(req.getParameter("reviewId"));
                    String newStatus = req.getParameter("newStatus");
                    if ("VISIBLE".equals(newStatus) || "HIDDEN".equals(newStatus)) {
                        String result = reviewService.toggleReviewStatus(reviewId, newStatus);
                        if ("SUCCESS".equalsIgnoreCase(result)) {
                            session.setAttribute("adminReviewFlash",
                                "HIDDEN".equals(newStatus) ? "Đánh giá đã được ẩn." : "Đánh giá đã được hiển thị lại.");
                        }
                    }
                    break;
                }
                case "delete": {
                    int reviewId = Integer.parseInt(req.getParameter("reviewId"));
                    String result = reviewService.deleteReview(reviewId);
                    if ("SUCCESS".equalsIgnoreCase(result)) {
                        session.setAttribute("adminReviewFlash", "Đánh giá đã được xóa.");
                    }
                    break;
                }
                default:
                    session.setAttribute("adminReviewFlash", "Hành động không hợp lệ.");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("adminReviewFlash", "ID đánh giá không hợp lệ.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            session.setAttribute("adminReviewFlash", "Đã xảy ra lỗi: " + e.getMessage());
        }

        // Preserve filter params on redirect
        StringBuilder redirectUrl = new StringBuilder("admin-reviews");
        String keyword = req.getParameter("keyword");
        String rating = req.getParameter("rating");
        String replyStatusParam = req.getParameter("replyStatus");
        boolean hasParam = false;
        if (keyword != null && !keyword.trim().isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("keyword=").append(java.net.URLEncoder.encode(keyword, "UTF-8"));
            hasParam = true;
        }
        if (rating != null && !rating.trim().isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("rating=").append(rating);
            hasParam = true;
        }
        if (replyStatusParam != null && !replyStatusParam.trim().isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("replyStatus=").append(replyStatusParam);
            hasParam = true;
        }

        res.sendRedirect(redirectUrl.toString());
    }
}
