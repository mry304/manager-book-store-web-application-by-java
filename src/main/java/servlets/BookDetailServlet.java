package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.bittercode.model.Book;
import com.bittercode.model.BookReview;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.service.BookReviewService;
import com.bittercode.service.impl.BookServiceImpl;
import com.bittercode.service.impl.BookReviewServiceImpl;
import com.bittercode.util.StoreUtil;

/**
 * Servlet xử lý UC03 (Xem chi tiết sách) và UC04 (Đánh giá sách).
 * Mapping: /book-detail
 */
public class BookDetailServlet extends HttpServlet {

    private final BookService bookService = new BookServiceImpl();
    private final BookReviewService reviewService = new BookReviewServiceImpl();

    private static final String[] COVER_COLORS = {
        "#047857", "#be123c", "#1d4ed8", "#b45309",
        "#6d28d9", "#0f766e", "#c2410c", "#1e293b"
    };

    // ========== doGet — Hiển thị trang chi tiết sách ==========
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=UTF-8");
        PrintWriter pw = res.getWriter();

        String barcode = req.getParameter("barcode");
        if (barcode == null || barcode.trim().isEmpty()) {
            res.sendRedirect("viewbook");
            return;
        }
        barcode = barcode.trim();

        // Lấy thông tin sách
        Book book;
        try {
            book = bookService.getBookById(barcode);
        } catch (Exception e) {
            e.printStackTrace();
            res.sendRedirect("viewbook");
            return;
        }
        if (book == null) {
            res.sendRedirect("viewbook");
            return;
        }

        // Lấy đánh giá
        List<BookReview> reviews;
        double avgRating;
        int reviewCount;
        try {
            reviews = reviewService.getReviewsByBarcode(barcode);
            avgRating = reviewService.getAverageRating(barcode);
            reviewCount = reviewService.getReviewCount(barcode);
        } catch (Exception e) {
            e.printStackTrace();
            reviews = java.util.Collections.emptyList();
            avgRating = 0;
            reviewCount = 0;
        }

        HttpSession session = req.getSession();
        boolean isCustomer = StoreUtil.isLoggedIn(UserRole.CUSTOMER, session);
        boolean isSeller   = StoreUtil.isLoggedIn(UserRole.SELLER, session);

        // Flash message sau khi gửi đánh giá
        String flashMsg = (String) session.getAttribute("reviewFlash");
        if (flashMsg != null) session.removeAttribute("reviewFlash");

        // --- Render Header ---
        if (isCustomer || isSeller) {
            String header = isSeller ? "SellerHome.html" : "CustomerHome.html";
            RequestDispatcher rd = req.getRequestDispatcher(header);
            rd.include(req, res);
            StoreUtil.setActiveTab(pw, "books", session);
        } else {
            // Guest — render header đơn giản
            pw.println("<!DOCTYPE html><html><head>");
            pw.println("<meta charset='UTF-8'>");
            pw.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            pw.println("<title>" + escapeHtml(book.getName()) + " - Online Book Store</title>");
            pw.println("<link rel='stylesheet' href='css/bookshelf-theme.css'>");
            pw.println("</head><body class='customer-bookshelf-body'>");
            pw.println("<div class='bookshelf-header-wrap'><nav class='navbar-bookshelf'>");
            pw.println("<a class='brand' href='viewbook'>&#128218; BookStore</a>");
            pw.println("<div style='margin-left:auto; display:flex; gap:8px;'>");
            pw.println("<a href='userlog' class='nav-pill-btn'>&#128100; Đăng nhập</a>");
            pw.println("<a href='userreg' class='nav-pill-btn'>Đăng ký</a>");
            pw.println("</div></nav></div>");
        }

        // === MAIN CONTENT ===
        String bookColor = getBookColor(barcode);
        pw.println("<main class='bookshelf-canvas' style='padding-top:28px;'>");
        pw.println("<div class='bookshelf-page-container' style='max-width:960px; margin:0 auto;'>");

        // ---- Breadcrumb ----
        pw.println("<div style='margin-bottom:18px; font-size:0.88rem; color:var(--text-secondary);'>");
        pw.println("  <a href='viewbook' style='color:var(--accent-primary); text-decoration:none; font-weight:600;'>&#8592; Quay lại danh sách</a>");
        pw.println("</div>");

        // ---- Flash message ----
        if (flashMsg != null) {
            pw.println("<div style='background:#dcfce7; border:1px solid #bbf7d0; border-radius:12px; padding:14px 20px; margin-bottom:20px; color:#15803d; font-weight:600; text-align:center;'>"
                    + escapeHtml(flashMsg) + "</div>");
        }

        // ---- Book Detail Card ----
        pw.println("<div class='bookshelf-page-card' style='box-shadow:var(--shadow-canvas);'>");
        pw.println("  <div style='display:grid; grid-template-columns:240px 1fr; gap:36px; align-items:start;'>");

        // Left — Book Cover
        pw.println("    <div style='text-align:center;'>");
        if (book.getImageUrl() != null && !book.getImageUrl().trim().isEmpty()) {
            pw.println("      <img src='" + escapeHtml(book.getImageUrl()) + "' alt='" + escapeHtml(book.getName())
                    + "' style='width:220px; max-height:330px; object-fit:cover; border-radius:12px; box-shadow:var(--shadow-book-standing);'>");
        } else {
            pw.println("      <div class='book-standing-3d' style='--book-color:" + bookColor
                    + "; width:220px; height:330px; margin:0 auto; padding:20px 17px 18px 26px; border-radius:7px 11px 8px 6px; "
                    + "box-shadow:17px 27px 22px rgba(63,47,31,.27), 5px 10px 8px rgba(63,47,31,.16);'>");
            pw.println("        <span class='book-kicker' style='font-size:.56rem;'>CHI TI&#7870;T</span>");
            pw.println("        <span class='book-title' style='font-size:2rem;'>" + escapeHtml(book.getName()) + "</span>");
            pw.println("        <span class='book-author' style='font-size:.76rem;'>" + escapeHtml(book.getAuthor()) + "</span>");
            pw.println("      </div>");
        }
        pw.println("    </div>");

        // Right — Book Info
        pw.println("    <div>");
        pw.println("      <h1 style='font-family:Playfair Display,serif; font-size:2rem; font-weight:700; margin:0 0 8px; color:var(--text-primary);'>"
                + escapeHtml(book.getName()) + "</h1>");
        pw.println("      <p style='color:var(--text-secondary); font-size:1rem; margin:0 0 6px;'>Tác giả: <strong style='color:var(--text-primary);'>"
                + escapeHtml(book.getAuthor()) + "</strong></p>");

        if (book.getPublisher() != null && !book.getPublisher().trim().isEmpty()) {
            pw.println("      <p style='color:var(--text-secondary); font-size:0.95rem; margin:0 0 6px;'>Nhà xuất bản: <strong>"
                    + escapeHtml(book.getPublisher()) + "</strong></p>");
        }

        // Rating summary
        pw.println("      <div style='display:flex; align-items:center; gap:8px; margin:12px 0;'>");
        pw.println("        <div style='color:var(--accent-primary); font-size:1.1rem; letter-spacing:2px;'>" + renderStars(avgRating) + "</div>");
        pw.println("        <span style='font-weight:700; color:var(--text-primary);'>" + String.format("%.1f", avgRating) + "</span>");
        pw.println("        <span style='color:var(--text-secondary); font-size:0.88rem;'>(" + reviewCount + " đánh giá)</span>");
        pw.println("      </div>");

        // Price
        pw.println("      <div style='font-size:1.8rem; font-weight:800; color:#2e6648; margin:16px 0;'>"
                + StoreUtil.formatPrice(book.getPrice()) + "</div>");

        // Stock
        if (book.getQuantity() > 0) {
            pw.println("      <p style='margin:0 0 8px;'><span style='background:#dcfce7; color:#15803d; padding:4px 12px; border-radius:999px; font-size:0.85rem; font-weight:700;'>Còn hàng: "
                    + book.getQuantity() + " cuốn</span></p>");
        } else {
            pw.println("      <p style='margin:0 0 8px;'><span class='badge-out-of-stock'>Tạm hết hàng</span></p>");
        }

        // Status
        if (book.getStatus() != null && !book.getStatus().trim().isEmpty() && !"ACTIVE".equalsIgnoreCase(book.getStatus())) {
            pw.println("      <p style='color:var(--text-muted); font-size:0.85rem; margin:4px 0;'>Trạng thái: " + escapeHtml(book.getStatus()) + "</p>");
        }

        // Description
        if (book.getDescription() != null && !book.getDescription().trim().isEmpty()) {
            pw.println("      <div style='margin-top:20px; padding-top:16px; border-top:1px solid rgba(197,137,64,.15);'>");
            pw.println("        <h3 style='font-family:Playfair Display,serif; font-size:1.1rem; font-weight:700; margin:0 0 10px;'>Mô tả chi tiết</h3>");
            pw.println("        <p style='color:var(--text-secondary); line-height:1.7; font-size:0.95rem; white-space:pre-line;'>"
                    + escapeHtml(book.getDescription()) + "</p>");
            pw.println("      </div>");
        }

        // Add to cart button
        if (isCustomer && book.getQuantity() > 0) {
            pw.println("      <form action='viewbook' method='post' style='margin-top:24px;'>");
            pw.println("        <input type='hidden' name='selectedBookId' value='" + escapeHtml(barcode) + "'>");
            pw.println("        <button type='submit' name='addToCart' class='btn-pill-buy' style='padding:12px 32px; font-size:1rem; font-weight:700; "
                    + "background:var(--accent-primary); color:#fff; border:none; border-radius:var(--radius-pill); "
                    + "cursor:pointer; box-shadow:0 6px 16px rgba(197,137,64,.28); transition:all .2s ease;'>"
                    + "&#128722; Thêm vào giỏ hàng</button>");
            pw.println("      </form>");
        } else if (!isCustomer && !isSeller) {
            pw.println("      <div style='margin-top:24px;'>");
            pw.println("        <a href='userlog' class='btn-pill-buy' style='padding:12px 32px; font-size:1rem; font-weight:700; "
                    + "background:var(--accent-primary); color:#fff; border:none; text-decoration:none;'>"
                    + "Đăng nhập để mua hàng</a>");
            pw.println("      </div>");
        }

        pw.println("    </div>"); // end right column
        pw.println("  </div>"); // end grid
        pw.println("</div>"); // end book detail card

        // ======== REVIEW SECTION ========
        pw.println("<div class='bookshelf-page-card' style='margin-top:28px; box-shadow:var(--shadow-canvas);'>");
        pw.println("  <header class='bookshelf-page-header'>");
        pw.println("    <h2>&#11088; Đánh giá & Nhận xét</h2>");
        pw.println("    <p>" + reviewCount + " đánh giá · Điểm trung bình: " + String.format("%.1f", avgRating) + " / 5.0</p>");
        pw.println("  </header>");

        // --- Average rating visual ---
        pw.println("  <div style='display:flex; align-items:center; gap:20px; margin-bottom:28px; padding:20px; background:var(--bg-board); border-radius:16px;'>");
        pw.println("    <div style='text-align:center;'>");
        pw.println("      <div style='font-size:3rem; font-weight:800; color:var(--accent-primary); line-height:1;'>" + String.format("%.1f", avgRating) + "</div>");
        pw.println("      <div style='color:var(--accent-primary); font-size:1.4rem; margin:4px 0;'>" + renderStars(avgRating) + "</div>");
        pw.println("      <div style='font-size:0.85rem; color:var(--text-secondary);'>" + reviewCount + " lượt đánh giá</div>");
        pw.println("    </div>");
        // Rating distribution bars (simplified)
        pw.println("    <div style='flex:1;'>");
        for (int star = 5; star >= 1; star--) {
            int count = countStarReviews(reviews, star);
            int pct = reviewCount > 0 ? (count * 100 / reviewCount) : 0;
            pw.println("      <div style='display:flex; align-items:center; gap:8px; margin:3px 0;'>");
            pw.println("        <span style='font-size:0.82rem; font-weight:700; min-width:38px;'>" + star + " &#9733;</span>");
            pw.println("        <div style='flex:1; height:8px; background:#e2e8f0; border-radius:4px; overflow:hidden;'>"
                    + "<div style='height:100%; width:" + pct + "%; background:var(--accent-primary); border-radius:4px;'></div></div>");
            pw.println("        <span style='font-size:0.78rem; color:var(--text-muted); min-width:28px;'>" + count + "</span>");
            pw.println("      </div>");
        }
        pw.println("    </div>");
        pw.println("  </div>");

        // --- Review Form ---
        if (isCustomer) {
            String username = StoreUtil.getCustomerUsername(session);
            pw.println("  <div style='background:#fffdf9; border:1px solid rgba(197,137,64,.18); border-radius:16px; padding:24px; margin-bottom:28px;'>");
            pw.println("    <h3 style='font-family:Playfair Display,serif; font-size:1.1rem; font-weight:700; margin:0 0 16px;'>&#9997; Viết đánh giá của bạn</h3>");
            pw.println("    <form action='book-detail' method='post'>");
            pw.println("      <input type='hidden' name='barcode' value='" + escapeHtml(barcode) + "'>");

            // Star rating selector
            pw.println("      <div style='margin-bottom:16px;'>");
            pw.println("        <label style='display:block; font-weight:600; margin-bottom:8px; font-size:0.9rem;'>Chọn số sao:</label>");
            pw.println("        <div class='star-rating-input' style='display:flex; gap:4px; font-size:2rem; cursor:pointer; direction:rtl; justify-content:flex-end;'>");
            for (int s = 5; s >= 1; s--) {
                pw.println("          <input type='radio' name='rating' value='" + s + "' id='star" + s + "' style='display:none;' " + (s == 5 ? "required" : "") + ">");
                pw.println("          <label for='star" + s + "' class='star-label' style='color:#cbd5e1; cursor:pointer; transition:color .15s;' "
                        + "onmouseover=\"highlightStars(this," + s + ")\" onmouseout=\"resetStars()\" onclick=\"selectStar(" + s + ")\">&#9733;</label>");
            }
            pw.println("        </div>");
            pw.println("      </div>");

            // Comment textarea
            pw.println("      <div style='margin-bottom:16px;'>");
            pw.println("        <label for='reviewComment' style='display:block; font-weight:600; margin-bottom:8px; font-size:0.9rem;'>Nội dung nhận xét:</label>");
            pw.println("        <textarea name='comment' id='reviewComment' rows='4' required "
                    + "style='width:100%; padding:12px 16px; border:1px solid #cbd5e1; border-radius:12px; font-family:inherit; font-size:0.95rem; resize:vertical; outline:none; transition:border .2s;' "
                    + "onfocus=\"this.style.borderColor='var(--accent-primary)'\" onblur=\"this.style.borderColor='#cbd5e1'\" "
                    + "placeholder='Chia sẻ cảm nhận của bạn về cuốn sách này...'></textarea>");
            pw.println("      </div>");

            pw.println("      <button type='submit' style='padding:11px 28px; border-radius:var(--radius-pill); background:var(--accent-primary); color:#fff; border:none; "
                    + "font-size:0.95rem; font-weight:700; cursor:pointer; box-shadow:0 4px 12px rgba(197,137,64,.25); transition:all .2s;'>"
                    + "Gửi đánh giá</button>");
            pw.println("    </form>");
            pw.println("  </div>");

            // Star interaction script
            pw.println("  <script>");
            pw.println("  var selectedRating = 0;");
            pw.println("  function highlightStars(el, n) {");
            pw.println("    var labels = document.querySelectorAll('.star-label');");
            pw.println("    labels.forEach(function(l) { l.style.color = '#cbd5e1'; });");
            pw.println("    for (var i = 0; i < labels.length; i++) {");
            pw.println("      var val = 5 - i;");
            pw.println("      if (val <= n) l = labels[i]; // direction rtl, index 0 = star 5");
            pw.println("    }");
            pw.println("    // Simpler: iterate and color based on value");
            pw.println("    labels.forEach(function(l, idx) {");
            pw.println("      var v = 5 - idx;");
            pw.println("      if (v <= n) l.style.color = 'var(--accent-primary)';");
            pw.println("    });");
            pw.println("  }");
            pw.println("  function resetStars() {");
            pw.println("    var labels = document.querySelectorAll('.star-label');");
            pw.println("    labels.forEach(function(l, idx) {");
            pw.println("      var v = 5 - idx;");
            pw.println("      l.style.color = v <= selectedRating ? 'var(--accent-primary)' : '#cbd5e1';");
            pw.println("    });");
            pw.println("  }");
            pw.println("  function selectStar(n) {");
            pw.println("    selectedRating = n;");
            pw.println("    document.getElementById('star' + n).checked = true;");
            pw.println("    resetStars();");
            pw.println("  }");
            pw.println("  </script>");

        } else if (!isSeller) {
            // Guest — thông báo cần đăng nhập
            pw.println("  <div style='background:#fef3c7; border:1px solid #fde68a; border-radius:12px; padding:20px; margin-bottom:28px; text-align:center;'>");
            pw.println("    <p style='margin:0 0 10px; color:#92400e; font-weight:600;'>&#128274; Bạn cần đăng nhập để viết đánh giá</p>");
            pw.println("    <a href='userlog' class='btn-pill-buy' style='background:var(--accent-primary); color:#fff; border:none; padding:10px 24px; text-decoration:none;'>"
                    + "Đăng nhập ngay</a>");
            pw.println("  </div>");
        }

        // --- Reviews List ---
        if (reviews.isEmpty()) {
            pw.println("  <div style='text-align:center; padding:40px 20px; color:var(--text-secondary);'>");
            pw.println("    <div style='font-size:2.5rem; margin-bottom:10px;'>&#128172;</div>");
            pw.println("    <p style='font-size:1rem;'>Chưa có đánh giá nào cho cuốn sách này. Hãy là người đầu tiên!</p>");
            pw.println("  </div>");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (BookReview review : reviews) {
                String displayName = review.getUserFullName();
                if (displayName == null || displayName.trim().isEmpty() || "null null".equals(displayName.trim())) {
                    displayName = review.getUsername();
                }
                String dateStr = review.getCreatedAt() != null ? sdf.format(review.getCreatedAt()) : "";

                pw.println("  <div style='border-bottom:1px solid rgba(197,137,64,.1); padding:18px 0;'>");
                pw.println("    <div style='display:flex; align-items:center; gap:12px; margin-bottom:8px;'>");
                // Avatar
                pw.println("      <div style='width:40px; height:40px; border-radius:50%; background:linear-gradient(135deg,#3d403d,#b98b63); "
                        + "display:grid; place-items:center; color:#fff; font-weight:700; font-size:1rem;'>"
                        + escapeHtml(displayName.substring(0, 1).toUpperCase()) + "</div>");
                pw.println("      <div>");
                pw.println("        <div style='font-weight:700; font-size:0.95rem;'>" + escapeHtml(displayName) + "</div>");
                pw.println("        <div style='font-size:0.8rem; color:var(--text-muted);'>" + dateStr + "</div>");
                pw.println("      </div>");
                pw.println("      <div style='margin-left:auto; color:var(--accent-primary); font-size:0.95rem; letter-spacing:1px;'>"
                        + renderStars(review.getRating()) + "</div>");
                pw.println("    </div>");
                pw.println("    <p style='margin:0; color:var(--text-primary); line-height:1.6; font-size:0.95rem; white-space:pre-line;'>"
                        + escapeHtml(review.getComment()) + "</p>");

                // --- Admin Reply (if exists) ---
                String adminReply = review.getAdminReply();
                if (adminReply != null && !adminReply.trim().isEmpty()) {
                    String replyDateStr = review.getRepliedAt() != null ? sdf.format(review.getRepliedAt()) : "";
                    String repliedBy = review.getRepliedBy() != null ? review.getRepliedBy() : "Admin";
                    pw.println("    <div style='margin-top:12px; margin-left:52px; background:linear-gradient(135deg,#f0fdf4,#ecfdf5); "
                            + "border-left:3px solid #22c55e; border-radius:0 12px 12px 0; padding:14px 18px;'>");
                    pw.println("      <div style='display:flex; align-items:center; gap:8px; margin-bottom:6px;'>");
                    pw.println("        <div style='width:24px; height:24px; border-radius:50%; background:linear-gradient(135deg,#22c55e,#16a34a); "
                            + "display:grid; place-items:center; color:#fff; font-size:0.65rem; font-weight:800;'>QT</div>");
                    pw.println("        <span style='font-weight:700; font-size:0.85rem; color:#15803d;'>Ph\u1EA3n h\u1ED3i t\u1EEB Qu\u1EA3n tr\u1ECB vi\u00EAn</span>");
                    if (!replyDateStr.isEmpty()) {
                        pw.println("        <span style='font-size:0.75rem; color:#64748b; margin-left:auto;'>" + replyDateStr + "</span>");
                    }
                    pw.println("      </div>");
                    pw.println("      <p style='margin:0; color:#166534; line-height:1.6; font-size:0.9rem; white-space:pre-line;'>"
                            + escapeHtml(adminReply) + "</p>");
                    pw.println("    </div>");
                }

                pw.println("  </div>");
            }
        }

        pw.println("</div>"); // end review card
        pw.println("</div>"); // end page container
        pw.println("</main>");

        // Responsive CSS override for mobile
        pw.println("<style>");
        pw.println("@media (max-width: 768px) {");
        pw.println("  .bookshelf-page-card > div[style*='grid-template-columns'] {");
        pw.println("    grid-template-columns: 1fr !important;");
        pw.println("    gap: 20px !important;");
        pw.println("  }");
        pw.println("}");
        pw.println("</style>");

        if (!isCustomer && !isSeller) {
            pw.println("</body></html>");
        }
    }

    // ========== doPost — Xử lý gửi đánh giá ==========
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String barcode = req.getParameter("barcode");

        HttpSession session = req.getSession();

        // Kiểm tra quyền: chỉ CUSTOMER được gửi đánh giá
        if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)) {
            res.sendRedirect("userlog");
            return;
        }

        if (barcode == null || barcode.trim().isEmpty()) {
            res.sendRedirect("viewbook");
            return;
        }
        barcode = barcode.trim();

        // Validate rating
        int rating;
        try {
            rating = Integer.parseInt(req.getParameter("rating"));
            if (rating < 1 || rating > 5) {
                session.setAttribute("reviewFlash", "Điểm đánh giá phải từ 1 đến 5 sao!");
                res.sendRedirect("book-detail?barcode=" + barcode);
                return;
            }
        } catch (NumberFormatException e) {
            session.setAttribute("reviewFlash", "Vui lòng chọn số sao đánh giá!");
            res.sendRedirect("book-detail?barcode=" + barcode);
            return;
        }

        // Validate comment
        String comment = req.getParameter("comment");
        if (comment == null || comment.trim().isEmpty()) {
            session.setAttribute("reviewFlash", "Vui lòng nhập nội dung nhận xét!");
            res.sendRedirect("book-detail?barcode=" + barcode);
            return;
        }

        // Lấy username từ session
        String username = StoreUtil.getCustomerUsername(session);
        if (username == null) {
            res.sendRedirect("userlog");
            return;
        }

        // Tạo và lưu review
        BookReview review = new BookReview();
        review.setBookBarcode(barcode);
        review.setUsername(username);
        review.setRating(rating);
        review.setComment(comment.trim());

        try {
            String result = reviewService.addReview(review);
            if ("SUCCESS".equalsIgnoreCase(result)) {
                session.setAttribute("reviewFlash", "Cảm ơn bạn đã đánh giá! Nhận xét của bạn đã được ghi nhận.");
            } else {
                session.setAttribute("reviewFlash", "Gửi đánh giá thất bại. Vui lòng thử lại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("reviewFlash", "Đã xảy ra lỗi khi gửi đánh giá.");
        }

        res.sendRedirect("book-detail?barcode=" + barcode);
    }

    // ========== Helper methods ==========

    /** Render N sao filled + sao rỗng dựa trên rating (hỗ trợ số thập phân) */
    private String renderStars(double rating) {
        StringBuilder sb = new StringBuilder();
        int full = (int) Math.round(rating);
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= full ? "&#9733;" : "&#9734;");
        }
        return sb.toString();
    }

    /** Đếm số review có đúng N sao */
    private int countStarReviews(List<BookReview> reviews, int star) {
        int c = 0;
        for (BookReview r : reviews) {
            if (r.getRating() == star) c++;
        }
        return c;
    }

    private String getBookColor(String barcode) {
        if (barcode == null || barcode.isEmpty()) return COVER_COLORS[0];
        return COVER_COLORS[Math.abs(barcode.hashCode()) % COVER_COLORS.length];
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#39;");
    }
}
