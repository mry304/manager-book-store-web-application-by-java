package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.constant.ResponseCode;
import com.bittercode.constant.db.BooksDBConstants;
import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.service.impl.BookServiceImpl;
import com.bittercode.util.StoreUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
public class UpdateBookServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UpdateBookServlet.class.getName());

    BookService bookService = new BookServiceImpl();

    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        PrintWriter pw = res.getWriter();
        res.setContentType("text/html; charset=UTF-8");

        if (!StoreUtil.isLoggedIn(UserRole.SELLER, req.getSession())) {
            RequestDispatcher rd = req.getRequestDispatcher("SellerLogin.html");
            rd.include(req, res);
            pw.println("<table class=\"tab\"><tr><td>Vui l\u00f2ng \u0111\u0103ng nh\u1eadp qu\u1ea3n tr\u1ecb \u0111\u1ec3 ti\u1ebfp t\u1ee5c!</td></tr></table>");
            return;
        }

        RequestDispatcher rd = req.getRequestDispatcher("SellerHome.html");
        rd.include(req, res);
        StoreUtil.setActiveTab(pw, "storebooks");
        pw.println("<main class=\"bookshelf-canvas\" style=\"padding-top:28px;\">");
        pw.println("  <div class=\"bookshelf-page-container\" style=\"max-width:640px; margin: 0 auto;\">");
        pw.println("    <div class=\"bookshelf-page-card\" style=\"box-shadow: var(--shadow-canvas);\">");

        try {
            if (req.getParameter("updateFormSubmitted") != null) {
                String bName = req.getParameter(BooksDBConstants.COLUMN_NAME);
                String bCode = req.getParameter(BooksDBConstants.COLUMN_BARCODE);
                String bAuthor = req.getParameter(BooksDBConstants.COLUMN_AUTHOR);
                double bPrice = Double.parseDouble(req.getParameter(BooksDBConstants.COLUMN_PRICE));
                int bQty = Integer.parseInt(req.getParameter(BooksDBConstants.COLUMN_QUANTITY));

                Book book = new Book(bCode, bName, bAuthor, bPrice, bQty);
                // Đọc các trường chi tiết bổ sung (UC03)
                String bPublisher = req.getParameter(BooksDBConstants.COLUMN_PUBLISHER);
                String bImageUrl = req.getParameter(BooksDBConstants.COLUMN_IMAGE_URL);
                String bDescription = req.getParameter(BooksDBConstants.COLUMN_DESCRIPTION);
                book.setPublisher(bPublisher != null ? bPublisher.trim() : null);
                book.setImageUrl(bImageUrl != null ? bImageUrl.trim() : null);
                book.setDescription(bDescription != null ? bDescription.trim() : null);
                book.setStatus("ACTIVE");

                String message = bookService.updateBook(book);
                if (ResponseCode.SUCCESS.name().equalsIgnoreCase(message)) {
                    pw.println("<div class='alert alert-success text-center' style='border-radius:12px; margin-bottom:20px;'>\u0110\u00e3 c\u1eadp nh\u1eadt th\u00f4ng tin s\u00e1ch \"" + bName + "\" th\u00e0nh c\u00f4ng!</div>");
                } else {
                    pw.println("<div class='alert alert-danger text-center' style='border-radius:12px; margin-bottom:20px;'>C\u1eadp nh\u1eadt th\u1ea5t b\u1ea1i. Vui l\u00f2ng th\u1eed l\u1ea1i!</div>");
                }
                pw.println("<div class='text-center mt-3'><a href='storebooks' class='nav-pill-btn' style='background:var(--accent-primary); color:#fff; border-color:var(--accent-primary);'>&larr; Quay L\u1ea1i Kho S\u00e1ch</a></div>");
                pw.println("    </div></div></main>");
                return;
            }

            String bookId = req.getParameter("bookId");

            if (bookId != null) {
                Book book = bookService.getBookById(bookId);
                showUpdateBookForm(pw, book);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            pw.println("<div class='alert alert-danger text-center' style='border-radius:12px;'>T\u1ea3i th\u00f4ng tin s\u00e1ch th\u1ea5t b\u1ea1i!</div>");
        }
        pw.println("    </div></div></main>");
    }

    private static String safeValue(String val) {
        if (val == null) return "";
        return val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                  .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static void showUpdateBookForm(PrintWriter pw, Book book) {
        String form = "<header class=\"bookshelf-page-header\" style=\"text-align:center;\">\r\n"
                + "      <h1>C\u1eadp Nh\u1eadt Th\u00f4ng Tin S\u00e1ch</h1>\r\n"
                + "      <p>Ch\u1ec9nh s\u1eeda chi ti\u1ebft \u0111\u1ea7u s\u00e1ch v\u00e0 \u0111i\u1ec1u ch\u1ec9nh s\u1ed1 l\u01b0\u1ee3ng t\u1ed3n kho</p>\r\n"
                + "    </header>\r\n"
                + "    <form action=\"updatebook\" method=\"post\">\r\n"
                + "      <div class=\"bookshelf-input-group\">\r\n"
                + "        <label for=\"bookCode\">M\u00e3 v\u1ea1ch (Barcode - Ch\u1ec9 \u0111\u1ecdc)</label>\r\n"
                + "        <input type=\"text\" name=\"barcode\" id=\"bookCode\" value=\"" + book.getBarcode() + "\" readonly style=\"background:var(--bg-board); cursor:not-allowed;\">\r\n"
                + "      </div>\r\n"
                + "      <div class=\"bookshelf-input-group\">\r\n"
                + "        <label for=\"bookName\">T\u1ef1a s\u00e1ch</label>\r\n"
                + "        <input type=\"text\" name=\"name\" id=\"bookName\" value=\"" + safeValue(book.getName()) + "\" required>\r\n"
                + "      </div>\r\n"
                + "      <div class=\"bookshelf-input-group\">\r\n"
                + "        <label for=\"bookAuthor\">T\u00e1c gi\u1ea3</label>\r\n"
                + "        <input type=\"text\" name=\"author\" id=\"bookAuthor\" value=\"" + safeValue(book.getAuthor()) + "\" required>\r\n"
                + "      </div>\r\n"
                + "      <div class=\"bookshelf-input-group\">\r\n"
                + "        <label for=\"bookPrice\">\u0110\u01a1n gi\u00e1 (VN\u0110)</label>\r\n"
                + "        <input type=\"number\" name=\"price\" id=\"bookPrice\" value=\"" + (long)book.getPrice() + "\" required min=\"1\" step=\"any\">\r\n"
                + "      </div>\r\n"
                + "      <div class=\"bookshelf-input-group\">\r\n"
                + "        <label for=\"bookQuantity\">S\u1ed1 l\u01b0\u1ee3ng t\u1ed3n kho</label>\r\n"
                + "        <input type=\"number\" name=\"quantity\" id=\"bookQuantity\" value=\"" + book.getQuantity() + "\" required min=\"0\">\r\n"
                + "      </div>\r\n"
                // --- Các trường bổ sung UC03 ---
                + "      <div class=\"bookshelf-input-group\">\r\n"
                + "        <label for=\"bookPublisher\">Nh\u00e0 xu\u1ea5t b\u1ea3n</label>\r\n"
                + "        <input type=\"text\" name=\"publisher\" id=\"bookPublisher\" value=\"" + safeValue(book.getPublisher()) + "\">\r\n"
                + "      </div>\r\n"
                + "      <div class=\"bookshelf-input-group\">\r\n"
                + "        <label for=\"bookImageUrl\">URL \u1ea2nh b\u00eca</label>\r\n"
                + "        <input type=\"text\" name=\"image_url\" id=\"bookImageUrl\" value=\"" + safeValue(book.getImageUrl()) + "\">\r\n"
                + "      </div>\r\n"
                + "      <div class=\"bookshelf-input-group\">\r\n"
                + "        <label for=\"bookDescription\">M\u00f4 t\u1ea3 chi ti\u1ebft</label>\r\n"
                + "        <textarea name=\"description\" id=\"bookDescription\" rows=\"5\" style=\"width:100%; padding:12px 16px; border:1px solid #cbd5e1; border-radius:12px; font-family:inherit; font-size:0.95rem; resize:vertical;\">"
                + safeValue(book.getDescription()) + "</textarea>\r\n"
                + "      </div>\r\n"
                // --- End UC03 fields ---
                + "      <button class=\"btn-auth-submit\" type=\"submit\" name=\"updateFormSubmitted\" value=\"true\" style=\"margin-top:12px;\">L\u01b0u Thay \u0110\u1ed5i</button>\r\n"
                + "      <div style=\"text-align:center; margin-top:16px;\"><a href=\"storebooks\" style=\"color:var(--text-secondary); text-decoration:none; font-size:0.9rem;\">&larr; H\u1ee7y b\u1ecf v\u00e0 quay l\u1ea1i kho s\u00e1ch</a></div>\r\n"
                + "    </form>\r\n";
        pw.println(form);
    }
}
