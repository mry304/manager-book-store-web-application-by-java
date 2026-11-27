package servlets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.bittercode.util.UploadConfig;

/**
 * Servlet phục vụ ảnh sách ký gửi từ thư mục UPLOAD_DIR với kiểm tra chặt chẽ tên UUID và path traversal.
 * URL: /consignment-images/*
 */
public class ConsignmentImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1 || !pathInfo.startsWith("/")) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = pathInfo.substring(1);
        Path safePath = UploadConfig.resolveSafeImagePath(fileName);
        if (safePath == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = UploadConfig.getContentTypeForFileName(fileName);
        res.setContentType(contentType);
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Cache-Control", "public, max-age=86400");
        res.setContentLengthLong(Files.size(safePath));

        Files.copy(safePath, res.getOutputStream());
    }
}
