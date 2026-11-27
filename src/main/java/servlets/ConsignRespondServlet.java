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

import com.bittercode.model.ConsignmentStatus;
import com.bittercode.model.StoreException;
import com.bittercode.model.UsedBook;
import com.bittercode.service.ConsignmentService;
import com.bittercode.service.impl.ConsignmentServiceImpl;
import com.bittercode.util.StoreUtil;
import com.bittercode.util.UploadConfig;

/**
 * Servlet xử lý bổ sung thông tin cho yêu cầu ký gửi ở trạng thái NEED_MORE_INFO.
 * URL: /consign-respond?id=...
 */
@MultipartConfig(
        fileSizeThreshold = 512 * 1024,
        maxFileSize = 2L * 1024L * 1024L,
        maxRequestSize = 7L * 1024L * 1024L
)
public class ConsignRespondServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ConsignRespondServlet.class.getName());
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

        Long id = parseId(req.getParameter("id"));
        if (id == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            UsedBook usedBook = consignmentService.getByIdForOwner(id, username);
            if (usedBook == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (usedBook.getConsignmentStatus() != ConsignmentStatus.NEED_MORE_INFO) {
                req.getSession().setAttribute("consignFlash",
                        "Yêu cầu #" + id + " hiện không ở trạng thái cần bổ sung thông tin.");
                res.sendRedirect(req.getContextPath() + "/my-consignments");
                return;
            }

            req.setAttribute("usedBook", usedBook);
            req.setAttribute("csrfToken", StoreUtil.getOrCreateCsrfToken(req.getSession()));
            req.getRequestDispatcher("/consign-respond.jsp").forward(req, res);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading consignment for respond id=" + id, e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
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
        List<String> savedNewImages = Collections.emptyList();
        UsedBook existing = null;

        try {
            Long id = parseId(req.getParameter("id"));
            String csrfToken = req.getParameter("csrfToken");
            form = ConsignmentServletHelper.readRawFormInput(req);

            if (id == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            existing = consignmentService.getByIdForOwner(id, username);
            if (existing == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (!StoreUtil.isValidCsrfToken(session, csrfToken)) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                forwardWithError(req, res, session, existing, form,
                        "Phiên làm việc không hợp lệ hoặc đã hết hạn. Vui lòng tải lại trang và thử lại.");
                return;
            }

            ConsignmentServletHelper.validateFormInput(form);
            savedNewImages = UploadConfig.saveValidatedConsignmentImages(req.getParts(), "images", false);

            List<String> oldImages = existing.getImageFileNames();
            consignmentService.respondToInfoRequest(
                    id,
                    username,
                    form.title,
                    form.author,
                    form.publisher,
                    form.description,
                    form.originalPrice,
                    form.resalePrice,
                    form.conditionPercent,
                    savedNewImages
            );

            if (!savedNewImages.isEmpty() && oldImages != null && !oldImages.isEmpty()) {
                UploadConfig.deleteSavedFilesQuietly(oldImages);
            }

            session.setAttribute("consignFlash",
                    "Đã gửi thông tin bổ sung cho yêu cầu #" + id + ". Quản trị viên sẽ xem xét lại.");
            res.sendRedirect(req.getContextPath() + "/my-consignments");
        } catch (IllegalStateException ex) {
            UploadConfig.deleteSavedFilesQuietly(savedNewImages);
            LOGGER.log(Level.WARNING, "Consign respond upload size limit exceeded", ex);
            if (existing == null) {
                res.sendRedirect(req.getContextPath() + "/my-consignments");
                return;
            }
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            forwardWithError(req, res, session, existing, form,
                    "Dung lượng ảnh vượt quá giới hạn 2MB/ảnh hoặc tổng dung lượng vượt quá 7MB.");
        } catch (StoreException se) {
            UploadConfig.deleteSavedFilesQuietly(savedNewImages);
            if (existing == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            res.setStatus(se.getStatusCode() >= 400 ? se.getStatusCode() : HttpServletResponse.SC_BAD_REQUEST);
            forwardWithError(req, res, session, existing, form,
                    ConsignmentServletHelper.toFriendlyErrorMessage(se, LOGGER));
        } catch (Exception e) {
            UploadConfig.deleteSavedFilesQuietly(savedNewImages);
            LOGGER.log(Level.SEVERE, "Unexpected error in ConsignRespondServlet.doPost", e);
            if (existing == null) {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            forwardWithError(req, res, session, existing, form,
                    "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.");
        }
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse res, HttpSession session,
                                  UsedBook existing, ConsignmentServletHelper.FormInput form, String errorMessage)
            throws ServletException, IOException {
        req.setAttribute("usedBook", existing);
        req.setAttribute("errorMsg", errorMessage);
        req.setAttribute("csrfToken", StoreUtil.getOrCreateCsrfToken(session));
        req.setAttribute("formTitle", form.title);
        req.setAttribute("formAuthor", form.author);
        req.setAttribute("formPublisher", form.publisher);
        req.setAttribute("formDescription", form.description);
        req.setAttribute("formOriginalPrice", form.originalPriceRaw);
        req.setAttribute("formResalePrice", form.resalePriceRaw);
        req.setAttribute("formConditionPercent", form.conditionPercentRaw);
        req.getRequestDispatcher("/consign-respond.jsp").forward(req, res);
    }

    private Long parseId(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            long val = Long.parseLong(raw.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
