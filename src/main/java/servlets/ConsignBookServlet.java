package servlets;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.bittercode.model.StoreException;
import com.bittercode.service.ConsignmentService;
import com.bittercode.service.impl.ConsignmentServiceImpl;
import com.bittercode.util.StoreUtil;
import com.bittercode.util.UploadConfig;

/**
 * Servlet xử lý form gửi yêu cầu ký gửi sách cũ của khách hàng.
 * URL: /consign
 */
@MultipartConfig(
        fileSizeThreshold = 512 * 1024,
        maxFileSize = 2L * 1024L * 1024L,
        maxRequestSize = 7L * 1024L * 1024L
)
public class ConsignBookServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ConsignBookServlet.class.getName());
    private static final long serialVersionUID = 1L;

    private final ConsignmentService consignmentService = new ConsignmentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=UTF-8");

        if (ConsignmentServletHelper.requireCustomer(req, res) == null) {
            return;
        }

        HttpSession session = req.getSession();
        req.setAttribute("csrfToken", StoreUtil.getOrCreateCsrfToken(session));
        req.getRequestDispatcher("/consign-book.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=UTF-8");

        String username = ConsignmentServletHelper.requireCustomer(req, res);
        if (username == null) {
            return;
        }

        HttpSession session = req.getSession();
        ConsignmentServletHelper.FormInput form = new ConsignmentServletHelper.FormInput();
        List<String> savedImages = Collections.emptyList();

        try {
            String csrfToken = req.getParameter("csrfToken");
            form = ConsignmentServletHelper.readRawFormInput(req);

            if (!StoreUtil.isValidCsrfToken(session, csrfToken)) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                forwardWithError(req, res, session, form,
                        "Phiên làm việc không hợp lệ hoặc đã hết hạn. Vui lòng tải lại trang và thử lại.");
                return;
            }

            ConsignmentServletHelper.validateFormInput(form);
            savedImages = UploadConfig.saveValidatedConsignmentImages(req.getParts(), "images", true);

            long usedBookId = consignmentService.submit(
                    username,
                    form.title,
                    form.author,
                    form.publisher,
                    form.description,
                    form.originalPrice,
                    form.resalePrice,
                    form.conditionPercent,
                    savedImages
            );

            session.setAttribute("consignFlash",
                    "Đã gửi yêu cầu ký gửi #" + usedBookId + " thành công! Quản trị viên sẽ kiểm duyệt sớm.");
            res.sendRedirect(req.getContextPath() + "/my-consignments");
        } catch (IllegalStateException ex) {
            UploadConfig.deleteSavedFilesQuietly(savedImages);
            LOGGER.log(Level.WARNING, "Consignment upload size limit exceeded", ex);
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            forwardWithError(req, res, session, form,
                    "Dung lượng ảnh vượt quá giới hạn 2MB/ảnh hoặc tổng dung lượng vượt quá 7MB.");
        } catch (StoreException se) {
            UploadConfig.deleteSavedFilesQuietly(savedImages);
            res.setStatus(se.getStatusCode() >= 400 ? se.getStatusCode() : HttpServletResponse.SC_BAD_REQUEST);
            forwardWithError(req, res, session, form, ConsignmentServletHelper.toFriendlyErrorMessage(se, LOGGER));
        } catch (Exception e) {
            UploadConfig.deleteSavedFilesQuietly(savedImages);
            LOGGER.log(Level.SEVERE, "Unexpected error in ConsignBookServlet.doPost", e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            forwardWithError(req, res, session, form, "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.");
        }
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse res, HttpSession session,
                                  ConsignmentServletHelper.FormInput form, String errorMessage)
            throws ServletException, IOException {
        req.setAttribute("errorMsg", errorMessage);
        req.setAttribute("csrfToken", StoreUtil.getOrCreateCsrfToken(session));
        req.setAttribute("formTitle", form.title);
        req.setAttribute("formAuthor", form.author);
        req.setAttribute("formPublisher", form.publisher);
        req.setAttribute("formDescription", form.description);
        req.setAttribute("formOriginalPrice", form.originalPriceRaw);
        req.setAttribute("formResalePrice", form.resalePriceRaw);
        req.setAttribute("formConditionPercent", form.conditionPercentRaw);
        req.getRequestDispatcher("/consign-book.jsp").forward(req, res);
    }
}
