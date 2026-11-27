package com.bittercode.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.servlet.http.Part;

import com.bittercode.constant.BookCoinPolicy;
import com.bittercode.model.StoreException;

/**
 * Cấu hình thư mục UPLOAD_DIR và tiện ích lưu/đọc ảnh ký gửi an toàn
 * (kiểm tra magic bytes, giới hạn dung lượng, chặn path traversal).
 */
public final class UploadConfig {
    private static final Logger LOGGER = Logger.getLogger(UploadConfig.class.getName());

    public static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024L * 1024L;
    public static final long MAX_REQUEST_SIZE_BYTES = 7L * 1024L * 1024L;

    private static final int MAGIC_HEADER_READ_BYTES = 12;
    private static final int IO_BUFFER_SIZE = 8192;
    private static final Pattern UUID_IMAGE_NAME_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png|webp)$");

    private UploadConfig() {
    }

    /**
     * Đọc UPLOAD_DIR từ biến môi trường / .env (qua EmailConfig), mặc định <user.home>/bookstore-uploads.
     */
    public static Path getUploadDirectory() throws IOException {
        String configured = System.getenv("UPLOAD_DIR");
        if (isBlank(configured)) {
            try {
                configured = EmailConfig.get("UPLOAD_DIR", null);
            } catch (Exception ignored) {
                configured = null;
            }
        }
        Path dir = isBlank(configured)
                ? Paths.get(System.getProperty("user.home"), "bookstore-uploads")
                : Paths.get(configured.trim());
        Path normalized = dir.toAbsolutePath().normalize();
        Files.createDirectories(normalized);
        return normalized;
    }

    /**
     * Kiểm tra định dạng thật bằng magic bytes, giới hạn số lượng & kích thước, lưu bằng tên UUID.
     */
    public static List<String> saveValidatedConsignmentImages(Collection<Part> parts, String fieldName, boolean required)
            throws StoreException {
        List<Part> imageParts = new ArrayList<>();
        if (parts != null) {
            for (Part part : parts) {
                if (part != null && fieldName.equals(part.getName()) && part.getSize() > 0) {
                    imageParts.add(part);
                }
            }
        }

        if (required && imageParts.size() < BookCoinPolicy.MIN_IMAGES) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Vui lòng tải lên ít nhất " + BookCoinPolicy.MIN_IMAGES + " ảnh sách (JPEG, PNG hoặc WEBP).");
        }
        if (imageParts.size() > BookCoinPolicy.MAX_IMAGES) {
            throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                    "Chỉ được tải lên tối đa " + BookCoinPolicy.MAX_IMAGES + " ảnh.");
        }

        List<String> savedFileNames = new ArrayList<>();
        if (imageParts.isEmpty()) {
            return savedFileNames;
        }

        Path uploadDir;
        try {
            uploadDir = getUploadDirectory();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not initialize UPLOAD_DIR", e);
            throw new StoreException(500, "INTERNAL_SERVER_ERROR", "Không thể khởi tạo thư mục lưu ảnh.");
        }

        try {
            for (Part part : imageParts) {
                if (part.getSize() > MAX_IMAGE_SIZE_BYTES) {
                    throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                            "Mỗi ảnh không được vượt quá 2MB.");
                }
                String savedName = saveSingleImage(part, uploadDir);
                savedFileNames.add(savedName);
            }
            return savedFileNames;
        } catch (StoreException se) {
            deleteSavedFilesQuietly(savedFileNames);
            throw se;
        } catch (IOException e) {
            deleteSavedFilesQuietly(savedFileNames);
            LOGGER.log(Level.SEVERE, "Failed saving uploaded consignment image", e);
            throw new StoreException(500, "INTERNAL_SERVER_ERROR", "Không thể lưu ảnh tải lên.");
        }
    }

    private static String saveSingleImage(Part part, Path uploadDir) throws IOException, StoreException {
        try (InputStream is = part.getInputStream()) {
            ByteArrayOutputStream headerOut = new ByteArrayOutputStream(MAGIC_HEADER_READ_BYTES);
            byte[] tiny = new byte[MAGIC_HEADER_READ_BYTES];
            int headerRead = 0;
            while (headerRead < MAGIC_HEADER_READ_BYTES) {
                int r = is.read(tiny, 0, MAGIC_HEADER_READ_BYTES - headerRead);
                if (r == -1) {
                    break;
                }
                headerOut.write(tiny, 0, r);
                headerRead += r;
            }
            byte[] headerBytes = headerOut.toByteArray();
            String ext = detectImageExtension(headerBytes);
            if (ext == null) {
                throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                        "Định dạng ảnh không hợp lệ. Chỉ chấp nhận file JPEG, PNG hoặc WEBP thực tế.");
            }

            String safeFileName = UUID.randomUUID().toString() + "." + ext;
            Path target = uploadDir.resolve(safeFileName).normalize();
            if (!target.startsWith(uploadDir)) {
                throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION, "Đường dẫn lưu ảnh không hợp lệ.");
            }

            long totalWritten = headerBytes.length;
            boolean writeSucceeded = false;
            try (OutputStream os = Files.newOutputStream(target)) {
                os.write(headerBytes);
                byte[] buf = new byte[IO_BUFFER_SIZE];
                int n;
                while ((n = is.read(buf)) != -1) {
                    totalWritten += n;
                    if (totalWritten > MAX_IMAGE_SIZE_BYTES) {
                        throw new StoreException(400, BookCoinPolicy.ERR_VALIDATION,
                                "Mỗi ảnh không được vượt quá 2MB.");
                    }
                    os.write(buf, 0, n);
                }
                writeSucceeded = true;
            } finally {
                if (!writeSucceeded) {
                    Files.deleteIfExists(target);
                }
            }
            return safeFileName;
        }
    }

    /**
     * Xác định định dạng ảnh thật qua magic bytes (chỉ JPEG, PNG, WEBP).
     */
    public static String detectImageExtension(byte[] header) {
        if (header == null) {
            return null;
        }
        // JPEG: FF D8 FF
        if (header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header.length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A) {
            return "png";
        }
        // WEBP: "RIFF" + 4 bytes + "WEBP"
        if (header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "webp";
        }
        return null;
    }

    /**
     * Giải quyết đường dẫn ảnh an toàn: chỉ chấp nhận <UUID>.(jpg|png|webp) nằm trong UPLOAD_DIR.
     */
    public static Path resolveSafeImagePath(String requestedFileName) {
        if (isBlank(requestedFileName)) {
            return null;
        }
        String trimmed = requestedFileName.trim();
        if (!UUID_IMAGE_NAME_PATTERN.matcher(trimmed).matches()) {
            return null;
        }
        try {
            Path uploadDir = getUploadDirectory();
            Path resolved = uploadDir.resolve(trimmed).normalize();
            if (!resolved.startsWith(uploadDir) || !Files.isRegularFile(resolved)) {
                return null;
            }
            return resolved;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error resolving consignment image path", e);
            return null;
        }
    }

    public static String getContentTypeForFileName(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    public static void deleteSavedFilesQuietly(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }
        Path uploadDir;
        try {
            uploadDir = getUploadDirectory();
        } catch (IOException e) {
            return;
        }
        for (String name : fileNames) {
            if (isBlank(name) || !UUID_IMAGE_NAME_PATTERN.matcher(name.trim()).matches()) {
                continue;
            }
            try {
                Path target = uploadDir.resolve(name.trim()).normalize();
                if (target.startsWith(uploadDir)) {
                    Files.deleteIfExists(target);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not delete consignment image file: " + name, e);
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
