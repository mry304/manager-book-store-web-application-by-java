package servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.bittercode.constant.BookCoinPolicy;
import com.bittercode.model.StoreException;
import com.bittercode.model.UserRole;
import com.bittercode.util.StoreUtil;

/**
 * Tiện ích dùng chung cho các Servlet ký gửi phía khách hàng.
 */
final class ConsignmentServletHelper {

    private ConsignmentServletHelper() {
    }

    static final class FormInput {
        String title = "";
        String author = "";
        String publisher = "";
        String description = "";
        String originalPriceRaw = "";
        String resalePriceRaw = "";
        String conditionPercentRaw = "";

        int originalPrice;
        int resalePrice;
        int conditionPercent;
    }

    static String requireCustomer(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)) {
            res.sendRedirect(req.getContextPath() + "/userlog");
            return null;
        }
        String username = StoreUtil.getCustomerUsername(session);
        if (username == null || username.trim().isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/userlog");
            return null;
        }
        return username.trim();
    }

    static FormInput readRawFormInput(HttpServletRequest req) {
        FormInput input = new FormInput();
        input.title = trimParam(req.getParameter("title"));
        input.author = trimParam(req.getParameter("author"));
        input.publisher = trimParam(req.getParameter("publisher"));
        input.description = trimParam(req.getParameter("description"));
        input.originalPriceRaw = trimParam(req.getParameter("originalPrice"));
        input.resalePriceRaw = trimParam(req.getParameter("resalePrice"));
        input.conditionPercentRaw = trimParam(req.getParameter("conditionPercent"));
        return input;
    }

    static void validateFormInput(FormInput input) throws StoreException {
        if (input.title.isEmpty() || input.title.length() > 255) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Tiêu đề sách là bắt buộc và tối đa 255 ký tự.");
        }
        if (input.author.isEmpty() || input.author.length() > 100) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Tên tác giả là bắt buộc và tối đa 100 ký tự.");
        }
        if (input.publisher.length() > 200) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Nhà xuất bản tối đa 200 ký tự.");
        }

        input.originalPrice = parsePositiveInt(input.originalPriceRaw,
                "Giá bìa gốc phải là số nguyên dương (VNĐ).");
        input.resalePrice = parsePositiveInt(input.resalePriceRaw,
                "Giá bán lại phải là số nguyên dương (VNĐ).");
        input.conditionPercent = parsePositiveInt(input.conditionPercentRaw,
                "Tình trạng sách phải là số nguyên từ 1 đến 100 (%).");

        if (input.conditionPercent < BookCoinPolicy.MIN_SUBMIT_CONDITION_PERCENT
                || input.conditionPercent > BookCoinPolicy.MAX_SUBMIT_CONDITION_PERCENT) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Tình trạng sách phải nằm trong khoảng "
                            + BookCoinPolicy.MIN_SUBMIT_CONDITION_PERCENT + "–"
                            + BookCoinPolicy.MAX_SUBMIT_CONDITION_PERCENT + "%.");
        }
    }

    static String toFriendlyErrorMessage(StoreException e, Logger logger) {
        if (e == null) {
            return "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.";
        }
        String code = e.getErrorCode();
        if (BookCoinPolicy.ERR_VALIDATION.equals(code)
                || BookCoinPolicy.ERR_FORBIDDEN.equals(code)
                || BookCoinPolicy.ERR_INVALID_STATE.equals(code)
                || BookCoinPolicy.ERR_APPROVAL_RULE.equals(code)) {
            return e.getErrorMessage();
        }
        if (BookCoinPolicy.ERR_NOT_FOUND.equals(code)) {
            return "Không tìm thấy yêu cầu ký gửi tương ứng.";
        }
        if (BookCoinPolicy.ERR_DEADLINE_EXPIRED.equals(code)) {
            return "Yêu cầu ký gửi đã quá hạn bổ sung thông tin.";
        }
        if (logger != null) {
            logger.log(Level.SEVERE, "Consignment service error code=" + code, e);
        }
        return "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.";
    }

    static String trimParam(String value) {
        return value == null ? "" : value.trim();
    }

    private static int parsePositiveInt(String raw, String errorMsg) throws StoreException {
        if (raw == null || raw.isEmpty()) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, errorMsg);
        }
        try {
            int parsed = Integer.parseInt(raw);
            if (parsed <= 0) {
                throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, errorMsg);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, errorMsg);
        }
    }
}
