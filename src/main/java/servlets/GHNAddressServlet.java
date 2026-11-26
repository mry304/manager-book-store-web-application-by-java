package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.bittercode.util.GHNClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Server-side proxy for GHN Master Data APIs (provinces, districts, wards).
 * Keeps GHN_TOKEN on the server and uses GHN IDs/Codes directly.
 */
public class GHNAddressServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json; charset=UTF-8");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        String servletPath = req.getServletPath() != null ? req.getServletPath() : "";
        String pathInfo = req.getPathInfo() != null ? req.getPathInfo() : "";
        String route = servletPath + pathInfo;
        String type = req.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
            if (route.contains("provinces")) type = "provinces";
            else if (route.contains("districts")) type = "districts";
            else if (route.contains("wards")) type = "wards";
        }

        try {
            JsonNode data;
            if ("districts".equalsIgnoreCase(type)) {
                int provinceId = Integer.parseInt(req.getParameter("provinceId").trim());
                data = GHNClient.getDistricts(provinceId);
            } else if ("wards".equalsIgnoreCase(type)) {
                int districtId = Integer.parseInt(req.getParameter("districtId").trim());
                data = GHNClient.getWards(districtId);
            } else {
                data = GHNClient.getProvinces();
            }
            out.write("{\"error\":0,\"data\":" + MAPPER.writeValueAsString(data) + "}");
        } catch (NumberFormatException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":1,\"message\":\"Tham số ID địa chỉ không hợp lệ\"}");
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Không thể tải dữ liệu GHN";
            out.write("{\"error\":1,\"message\":\"" + msg + "\"}");
        }
    }
}
