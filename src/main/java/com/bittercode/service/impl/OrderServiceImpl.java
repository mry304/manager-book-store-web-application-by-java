package com.bittercode.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.bittercode.constant.ResponseCode;
import com.bittercode.model.Cart;
import com.bittercode.model.Order;
import com.bittercode.model.OrderDetail;
import com.bittercode.model.OrderStatus;
import com.bittercode.model.StoreException;
import com.bittercode.service.OrderService;
import com.bittercode.util.DBUtil;
import com.bittercode.util.GHNClient;
import com.bittercode.util.OrderEmailService;

public class OrderServiceImpl implements OrderService {
    private static volatile boolean orderCodeChecked = false;
    private static volatile boolean ghnSchemaChecked = false;

    private void ensureGhnSchema(Connection con) {
        if (ghnSchemaChecked) return;
        synchronized (OrderServiceImpl.class) {
            if (ghnSchemaChecked) return;
            String[] alterStatements = {
                "ALTER TABLE orders ADD COLUMN receiver_name VARCHAR(150) NULL",
                "ALTER TABLE orders ADD COLUMN receiver_phone VARCHAR(20) NULL",
                "ALTER TABLE orders ADD COLUMN to_province_id INT NULL",
                "ALTER TABLE orders ADD COLUMN to_district_id INT NULL",
                "ALTER TABLE orders ADD COLUMN to_ward_code VARCHAR(30) NULL",
                "ALTER TABLE orders ADD COLUMN shipping_fee DECIMAL(12,2) DEFAULT 0",
                "ALTER TABLE orders ADD COLUMN ghn_order_code VARCHAR(50) UNIQUE NULL",
                "ALTER TABLE orders ADD COLUMN ghn_status VARCHAR(50) NULL",
                "ALTER TABLE orders ADD COLUMN ghn_updated_at TIMESTAMP NULL"
            };
            for (String sql : alterStatements) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.executeUpdate();
                } catch (SQLException ignored) {
                    // Column already exists
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ghn_webhook_logs (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ghn_order_code VARCHAR(50) NOT NULL, " +
                    "ghn_status VARCHAR(50) NOT NULL, " +
                    "event_time VARCHAR(64) NULL, " +
                    "raw_payload TEXT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")) {
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            ghnSchemaChecked = true;
        }
    }

    private void ensureOrderCodeColumn(Connection con) {
        if (orderCodeChecked) return;
        synchronized (OrderServiceImpl.class) {
            if (orderCodeChecked) return;
            try (PreparedStatement ps = con.prepareStatement(
                    "ALTER TABLE orders ADD COLUMN order_code BIGINT UNIQUE NULL")) {
                ps.executeUpdate();
            } catch (SQLException ignored) {
                // Column already exists or another thread created it
            }
            orderCodeChecked = true;
        }
    }

    @Override
    public String createOrderWithCode(String username, List<Cart> cartItems, double totalAmount,
                                      String shippingEmail, String shippingAddress, String paymentMethod,
                                      long orderCode) throws StoreException {
        Connection con = DBUtil.getConnection();
        ensureOrderCodeColumn(con);
        String orderId = "ORD" + orderCode;
        String validUsername = resolveUsername(con, username);

        String insertOrderQuery = "INSERT INTO orders (order_id, order_code, username, total_amount, status, shipping_email, shipping_address, payment_method) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertDetailQuery = "INSERT INTO order_details (order_id, book_barcode, quantity, amount) VALUES (?, ?, ?, ?)";

        try {
            con.setAutoCommit(false);

            try (PreparedStatement psOrder = con.prepareStatement(insertOrderQuery)) {
                psOrder.setString(1, orderId);
                psOrder.setLong(2, orderCode);
                psOrder.setString(3, validUsername);
                psOrder.setDouble(4, totalAmount);
                psOrder.setString(5, OrderStatus.PENDING.name());
                psOrder.setString(6, normalizeOptionalValue(shippingEmail));
                psOrder.setString(7, normalizeOptionalValue(shippingAddress));
                psOrder.setString(8, normalizeOptionalValue(paymentMethod));
                psOrder.executeUpdate();
            }

            try (PreparedStatement psDetail = con.prepareStatement(insertDetailQuery)) {
                for (Cart cart : cartItems) {
                    psDetail.setString(1, orderId);
                    psDetail.setString(2, cart.getBook().getBarcode());
                    psDetail.setInt(3, cart.getQuantity());
                    psDetail.setDouble(4, cart.getBook().getPrice() * cart.getQuantity());
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            con.commit();
            return orderId;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Order getOrderByOrderCode(long orderCode) throws StoreException {
        Connection con = DBUtil.getConnection();
        ensureOrderCodeColumn(con);
        String query = "SELECT * FROM orders WHERE order_code = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, orderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderDetailsByOrderId(order.getOrderId(), con));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public boolean confirmPaymentAndDeductStock(long orderCode) throws StoreException {
        Connection con = DBUtil.getConnection();
        ensureOrderCodeColumn(con);
        Order order = getOrderByOrderCode(orderCode);
        if (order == null) {
            return false;
        }

        // Idempotent: If order is already confirmed or further in lifecycle, return true
        if (OrderStatus.CONFIRMED.name().equalsIgnoreCase(order.getStatus()) ||
            OrderStatus.SHIPPING.name().equalsIgnoreCase(order.getStatus()) ||
            OrderStatus.COMPLETED.name().equalsIgnoreCase(order.getStatus())) {
            return true;
        }

        if (!OrderStatus.PENDING.name().equalsIgnoreCase(order.getStatus())) {
            return false;
        }

        String updateOrderSql = "UPDATE orders SET status = 'CONFIRMED' WHERE order_code = ? AND status = 'PENDING'";
        String updateStockSql = "UPDATE books SET quantity = GREATEST(0, quantity - ?) WHERE barcode = ?";

        try {
            con.setAutoCommit(false);

            int updatedRows;
            try (PreparedStatement psOrder = con.prepareStatement(updateOrderSql)) {
                psOrder.setLong(1, orderCode);
                updatedRows = psOrder.executeUpdate();
            }

            if (updatedRows > 0) {
                List<OrderDetail> items = order.getItems();
                if (items == null || items.isEmpty()) {
                    items = getOrderDetailsByOrderId(order.getOrderId(), con);
                }

                if (items != null && !items.isEmpty()) {
                    try (PreparedStatement psStock = con.prepareStatement(updateStockSql)) {
                        for (OrderDetail item : items) {
                            psStock.setInt(1, item.getQuantity());
                            psStock.setString(2, item.getBookBarcode());
                            psStock.addBatch();
                        }
                        psStock.executeBatch();
                    }
                }

                con.commit();
                order.setStatus(OrderStatus.CONFIRMED.name());
                // Send confirmation email asynchronously
                try {
                    OrderEmailService.getInstance().sendOrderConfirmationEmail(order);
                } catch (Exception ex) {
                    System.err.println("[OrderService] Email notification error: " + ex.getMessage());
                }
                return true;
            } else {
                con.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public String createOrder(String username, List<Cart> cartItems, double totalAmount) throws StoreException {
        return createOrder(username, cartItems, totalAmount, null, null, null);
    }

    @Override
    public String createOrder(String username, List<Cart> cartItems, double totalAmount,
                              String shippingEmail, String shippingAddress, String paymentMethod) throws StoreException {
        Connection con = DBUtil.getConnection();
        String orderId = "ORD" + System.currentTimeMillis();
        String validUsername = resolveUsername(con, username);

        String insertOrderQuery = "INSERT INTO orders (order_id, username, total_amount, status, shipping_email, shipping_address, payment_method) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertDetailQuery = "INSERT INTO order_details (order_id, book_barcode, quantity, amount) VALUES (?, ?, ?, ?)";

        try {
            con.setAutoCommit(false);

            try (PreparedStatement psOrder = con.prepareStatement(insertOrderQuery)) {
                psOrder.setString(1, orderId);
                psOrder.setString(2, validUsername);
                psOrder.setDouble(3, totalAmount);
                psOrder.setString(4, OrderStatus.PENDING.name());
                psOrder.setString(5, normalizeOptionalValue(shippingEmail));
                psOrder.setString(6, normalizeOptionalValue(shippingAddress));
                psOrder.setString(7, normalizeOptionalValue(paymentMethod));
                psOrder.executeUpdate();
            }

            try (PreparedStatement psDetail = con.prepareStatement(insertDetailQuery)) {
                for (Cart cart : cartItems) {
                    psDetail.setString(1, orderId);
                    psDetail.setString(2, cart.getBook().getBarcode());
                    psDetail.setInt(3, cart.getQuantity());
                    psDetail.setDouble(4, cart.getBook().getPrice() * cart.getQuantity());
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            con.commit();
            return orderId;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Order getOrderById(String orderId) throws StoreException {
        Connection con = DBUtil.getConnection();
        String query = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderDetailsByOrderId(orderId, con));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public List<Order> getOrdersByUsername(String username) throws StoreException {
        return getOrdersByUsername(username, null);
    }

    @Override
    public List<Order> getOrdersByUsername(String username, String statusFilter) throws StoreException {
        checkAndAutoCompleteOrders();

        Connection con = DBUtil.getConnection();
        List<Order> orders = new ArrayList<>();
        String validUsername = resolveUsername(con, username);

        StringBuilder query = new StringBuilder("SELECT * FROM orders WHERE username = ? ");
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            query.append("AND status = ? ");
        }
        query.append("ORDER BY order_date DESC");

        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            ps.setString(1, validUsername);
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                ps.setString(2, statusFilter.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderDetailsByOrderId(order.getOrderId(), con));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return orders;
    }

    @Override
    public List<Order> getOrdersByUsername(String username, String statusFilter, int page, int pageSize) throws StoreException {
        return getOrdersPage(username, statusFilter, page, pageSize, true);
    }

    @Override
    public int getOrderCountByUsername(String username, String statusFilter) throws StoreException {
        Connection con = DBUtil.getConnection();
        String validUsername = resolveUsername(con, username);
        return getOrderCountInternal(con, validUsername, statusFilter, true);
    }

    @Override
    public List<Order> getAllOrders() throws StoreException {
        return getAllOrders(null);
    }

    @Override
    public List<Order> getAllOrders(String statusFilter) throws StoreException {
        checkAndAutoCompleteOrders();

        Connection con = DBUtil.getConnection();
        List<Order> orders = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM orders ");
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            query.append("WHERE status = ? ");
        }
        query.append("ORDER BY order_date DESC");

        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                ps.setString(1, statusFilter.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderDetailsByOrderId(order.getOrderId(), con));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return orders;
    }

    @Override
    public List<Order> getAllOrders(String statusFilter, int page, int pageSize) throws StoreException {
        return getOrdersPage(null, statusFilter, page, pageSize, false);
    }

    @Override
    public int getOrderCount(String statusFilter) throws StoreException {
        return getOrderCountInternal(DBUtil.getConnection(), null, statusFilter, false);
    }

    @Override
    public List<Order> searchAllOrders(String statusFilter, String keyword, String dateFrom, String dateTo,
                                       int page, int pageSize) throws StoreException {
        checkAndAutoCompleteOrders();
        Connection con = DBUtil.getConnection();
        List<Order> orders = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT DISTINCT o.* FROM orders o LEFT JOIN order_details od ON od.order_id = o.order_id LEFT JOIN books b ON b.barcode = od.book_barcode WHERE 1=1 ");
        List<String> values = appendSearchConditions(query, statusFilter, keyword, dateFrom, dateTo);
        query.append(" ORDER BY o.order_date DESC LIMIT ? OFFSET ?");
        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            int index = bindSearchValues(ps, values);
            ps.setInt(index++, Math.max(1, pageSize));
            ps.setInt(index, (Math.max(1, page) - 1) * Math.max(1, pageSize));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderDetailsByOrderId(order.getOrderId(), con));
                    orders.add(order);
                }
            }
            return orders;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public int getSearchOrderCount(String statusFilter, String keyword, String dateFrom, String dateTo) throws StoreException {
        Connection con = DBUtil.getConnection();
        StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT o.order_id) FROM orders o LEFT JOIN order_details od ON od.order_id = o.order_id LEFT JOIN books b ON b.barcode = od.book_barcode WHERE 1=1 ");
        List<String> values = appendSearchConditions(query, statusFilter, keyword, dateFrom, dateTo);
        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            bindSearchValues(ps, values);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> appendSearchConditions(StringBuilder query, String statusFilter, String keyword, String dateFrom, String dateTo) {
        List<String> values = new ArrayList<>();
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            query.append("AND o.status = ? ");
            values.add(statusFilter.trim().toUpperCase());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("AND (o.order_id LIKE ? OR o.username LIKE ? OR b.name LIKE ?) ");
            String term = "%" + keyword.trim() + "%";
            values.add(term); values.add(term); values.add(term);
        }
        if (dateFrom != null && dateFrom.matches("\\d{4}-\\d{2}-\\d{2}")) { query.append("AND DATE(o.order_date) >= ? "); values.add(dateFrom); }
        if (dateTo != null && dateTo.matches("\\d{4}-\\d{2}-\\d{2}")) { query.append("AND DATE(o.order_date) <= ? "); values.add(dateTo); }
        return values;
    }

    private int bindSearchValues(PreparedStatement ps, List<String> values) throws SQLException {
        int index = 1;
        for (String value : values) ps.setString(index++, value);
        return index;
    }

    private List<Order> getOrdersPage(String username, String statusFilter, int page, int pageSize, boolean byUsername) throws StoreException {
        checkAndAutoCompleteOrders();
        Connection con = DBUtil.getConnection();
        List<Order> orders = new ArrayList<>();
        String validUsername = byUsername ? resolveUsername(con, username) : null;
        boolean hasStatus = statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter);
        StringBuilder query = new StringBuilder("SELECT * FROM orders ");
        if (byUsername) query.append("WHERE username = ? ");
        if (hasStatus) query.append(byUsername ? "AND status = ? " : "WHERE status = ? ");
        query.append("ORDER BY order_date DESC LIMIT ? OFFSET ?");

        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            int index = 1;
            if (byUsername) ps.setString(index++, validUsername);
            if (hasStatus) ps.setString(index++, statusFilter.trim().toUpperCase());
            ps.setInt(index++, Math.max(1, pageSize));
            ps.setInt(index, (Math.max(1, page) - 1) * Math.max(1, pageSize));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderDetailsByOrderId(order.getOrderId(), con));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return orders;
    }

    private int getOrderCountInternal(Connection con, String username, String statusFilter, boolean byUsername) throws StoreException {
        boolean hasStatus = statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter);
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM orders ");
        if (byUsername) query.append("WHERE username = ? ");
        if (hasStatus) query.append(byUsername ? "AND status = ?" : "WHERE status = ?");
        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            int index = 1;
            if (byUsername) ps.setString(index++, username);
            if (hasStatus) ps.setString(index, statusFilter.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean cancelOrderByCustomer(String orderId, String username, String cancelReason) throws StoreException {
        Connection con = DBUtil.getConnection();
        String validUsername = resolveUsername(con, username);

        // Verify order exists, belongs to customer and is in PENDING state
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new StoreException("Đơn hàng không tồn tại");
        }
        if (!order.getUsername().equalsIgnoreCase(validUsername)) {
            throw new StoreException("Bạn không có quyền thao tác trên đơn hàng này");
        }
        if (!OrderStatus.PENDING.name().equalsIgnoreCase(order.getStatus())) {
            throw new StoreException("Chỉ có thể hủy đơn hàng khi đang ở trạng thái 'Chờ xác nhận'");
        }
        cancelGhnOrderIfPresent(order);

        String updateOrderSql = "UPDATE orders SET status = 'CANCELLED', cancelled_by = 'CUSTOMER', " +
                                "cancel_reason = ?, cancelled_at = CURRENT_TIMESTAMP WHERE order_id = ? AND status = 'PENDING'";
        String restoreStockSql = "UPDATE books SET quantity = quantity + ? WHERE barcode = ?";

        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(updateOrderSql)) {
                ps.setString(1, cancelReason != null && !cancelReason.trim().isEmpty() ? cancelReason.trim() : "Khách hàng hủy đơn");
                ps.setString(2, orderId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    return false;
                }
            }

            // Restore book inventory
            List<OrderDetail> items = order.getItems();
            if (items != null && !items.isEmpty()) {
                try (PreparedStatement psStock = con.prepareStatement(restoreStockSql)) {
                    for (OrderDetail item : items) {
                        psStock.setInt(1, item.getQuantity());
                        psStock.setString(2, item.getBookBarcode());
                        psStock.addBatch();
                    }
                    psStock.executeBatch();
                }
            }

            con.commit();
            OrderEmailService.getInstance().sendOrderCancelledEmail(order,
                    cancelReason != null && !cancelReason.trim().isEmpty() ? cancelReason.trim() : "Khách hàng hủy đơn",
                    "CUSTOMER");
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean completeOrderByCustomer(String orderId, String username) throws StoreException {
        Connection con = DBUtil.getConnection();
        String validUsername = resolveUsername(con, username);

        Order order = getOrderById(orderId);
        if (order == null) {
            throw new StoreException("Đơn hàng không tồn tại");
        }
        if (!order.getUsername().equalsIgnoreCase(validUsername)) {
            throw new StoreException("Bạn không có quyền thao tác trên đơn hàng này");
        }
        if (!OrderStatus.SHIPPING.name().equalsIgnoreCase(order.getStatus())) {
            throw new StoreException("Chỉ có thể xác nhận nhận hàng khi đơn hàng đang ở trạng thái 'Đang giao hàng'");
        }

        String sql = "UPDATE orders SET status = 'COMPLETED' WHERE order_id = ? AND status = 'SHIPPING'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean updateOrderStatusBySeller(String orderId, String newStatus, String cancelReason) throws StoreException {
        if (newStatus == null) return false;
        newStatus = newStatus.trim().toUpperCase();

        Order order = getOrderById(orderId);
        if (order == null) {
            throw new StoreException("Đơn hàng không tồn tại");
        }

        Connection con = DBUtil.getConnection();

        if (OrderStatus.CONFIRMED.name().equals(newStatus)) {
            if (!OrderStatus.PENDING.name().equalsIgnoreCase(order.getStatus())) {
                throw new StoreException("Chỉ có thể duyệt đơn khi đơn đang ở trạng thái 'Chờ xác nhận'");
            }
            String sql = "UPDATE orders SET status = 'CONFIRMED' WHERE order_id = ? AND status = 'PENDING'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, orderId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            }
        } else if (OrderStatus.SHIPPING.name().equals(newStatus)) {
            if (!OrderStatus.CONFIRMED.name().equalsIgnoreCase(order.getStatus())) {
                throw new StoreException("Chỉ có thể giao hàng cho đơn đã xác nhận");
            }
            String sql = "UPDATE orders SET status = 'SHIPPING', shipped_at = CURRENT_TIMESTAMP WHERE order_id = ? AND status = 'CONFIRMED'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, orderId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            }
        } else if (OrderStatus.COMPLETED.name().equals(newStatus)) {
            if (!OrderStatus.SHIPPING.name().equalsIgnoreCase(order.getStatus())) {
                throw new StoreException("Chỉ có thể hoàn thành khi đơn hàng đang giao");
            }
            String sql = "UPDATE orders SET status = 'COMPLETED' WHERE order_id = ? AND status = 'SHIPPING'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, orderId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            }
        } else if (OrderStatus.CANCELLED.name().equals(newStatus)) {
            if (!OrderStatus.PENDING.name().equalsIgnoreCase(order.getStatus()) && !OrderStatus.CONFIRMED.name().equalsIgnoreCase(order.getStatus())) {
                throw new StoreException("Người bán chỉ có thể hủy đơn khi đơn ở trạng thái 'Chờ xác nhận' hoặc 'Đã xác nhận'");
            }
            cancelGhnOrderIfPresent(order);

            String updateOrderSql = "UPDATE orders SET status = 'CANCELLED', cancelled_by = 'SELLER', " +
                                    "cancel_reason = ?, cancelled_at = CURRENT_TIMESTAMP WHERE order_id = ? AND status IN ('PENDING', 'CONFIRMED')";
            String restoreStockSql = "UPDATE books SET quantity = quantity + ? WHERE barcode = ?";

            try {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(updateOrderSql)) {
                    ps.setString(1, cancelReason != null && !cancelReason.trim().isEmpty() ? cancelReason.trim() : "Người bán hủy đơn");
                    ps.setString(2, orderId);
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        con.rollback();
                        return false;
                    }
                }

                // Restore book inventory
                List<OrderDetail> items = order.getItems();
                if (items != null && !items.isEmpty()) {
                    try (PreparedStatement psStock = con.prepareStatement(restoreStockSql)) {
                        for (OrderDetail item : items) {
                            psStock.setInt(1, item.getQuantity());
                            psStock.setString(2, item.getBookBarcode());
                            psStock.addBatch();
                        }
                        psStock.executeBatch();
                    }
                }

            con.commit();
            OrderEmailService.getInstance().sendOrderCancelledEmail(order,
                    cancelReason != null && !cancelReason.trim().isEmpty() ? cancelReason.trim() : "Người bán hủy đơn",
                    "SELLER");
            return true;
            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new StoreException("Trạng thái không hợp lệ: " + newStatus);
        }
    }

    @Override
    public int checkAndAutoCompleteOrders() throws StoreException {
        // Auto-complete orders that have been in SHIPPING status for more than 3 days
        String sql = "UPDATE orders SET status = 'COMPLETED' " +
                     "WHERE status = 'SHIPPING' AND shipped_at IS NOT NULL " +
                     "AND shipped_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY)";
        try {
            Connection con = DBUtil.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            // Log and do not break caller flow
            e.printStackTrace();
            return 0;
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getString("order_id"));
        order.setUsername(rs.getString("username"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));

        try {
            order.setCancelReason(rs.getString("cancel_reason"));
        } catch (SQLException ignored) {}

        try {
            order.setCancelledBy(rs.getString("cancelled_by"));
        } catch (SQLException ignored) {}

        try {
            order.setCancelledAt(rs.getTimestamp("cancelled_at"));
        } catch (SQLException ignored) {}

        try {
            order.setShippedAt(rs.getTimestamp("shipped_at"));
        } catch (SQLException ignored) {}

        try {
            order.setShippingEmail(rs.getString("shipping_email"));
        } catch (SQLException ignored) {}

        try {
            order.setShippingAddress(rs.getString("shipping_address"));
        } catch (SQLException ignored) {}

        try {
            order.setPaymentMethod(rs.getString("payment_method"));
        } catch (SQLException ignored) {}
        try {
            long code = rs.getLong("order_code");
            if (!rs.wasNull()) {
                order.setOrderCode(code);
            }
        } catch (SQLException ignored) {}
        try {
            order.setReceiverName(rs.getString("receiver_name"));
        } catch (SQLException ignored) {}
        try {
            order.setReceiverPhone(rs.getString("receiver_phone"));
        } catch (SQLException ignored) {}
        try {
            int pId = rs.getInt("to_province_id");
            if (!rs.wasNull()) order.setToProvinceId(pId);
        } catch (SQLException ignored) {}
        try {
            int dId = rs.getInt("to_district_id");
            if (!rs.wasNull()) order.setToDistrictId(dId);
        } catch (SQLException ignored) {}
        try {
            order.setToWardCode(rs.getString("to_ward_code"));
        } catch (SQLException ignored) {}
        try {
            order.setShippingFee(rs.getDouble("shipping_fee"));
        } catch (SQLException ignored) {}
        try {
            order.setGhnOrderCode(rs.getString("ghn_order_code"));
        } catch (SQLException ignored) {}
        try {
            order.setGhnStatus(rs.getString("ghn_status"));
        } catch (SQLException ignored) {}
        try {
            order.setGhnUpdatedAt(rs.getTimestamp("ghn_updated_at"));
        } catch (SQLException ignored) {}

        return order;
    }

    private List<OrderDetail> getOrderDetailsByOrderId(String orderId, Connection con) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        String query = "SELECT od.*, b.name AS book_name FROM order_details od " +
                       "LEFT JOIN books b ON od.book_barcode = b.barcode " +
                       "WHERE od.order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = new OrderDetail();
                    detail.setId(rs.getInt("id"));
                    detail.setOrderId(rs.getString("order_id"));
                    detail.setBookBarcode(rs.getString("book_barcode"));
                    detail.setBookName(rs.getString("book_name"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setAmount(rs.getDouble("amount"));
                    details.add(detail);
                }
            }
        }
        return details;
    }

    private String resolveUsername(Connection con, String username) {
        if (username == null || username.trim().isEmpty()) {
            return username;
        }
        String query = "SELECT username FROM users WHERE username = ? OR mailid = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }

    private String normalizeOptionalValue(String value) {
        return value != null && !value.trim().isEmpty() ? value.trim() : null;
    }

    private void cancelGhnOrderIfPresent(Order order) throws StoreException {
        if (order.getGhnOrderCode() == null || order.getGhnOrderCode().trim().isEmpty()) {
            return;
        }
        try {
            GHNClient.cancelShippingOrder(order.getGhnOrderCode().trim());
        } catch (Exception e) {
            throw new StoreException("Không thể hủy vận đơn GHN (" + order.getGhnOrderCode() + "): " + e.getMessage());
        }
    }

    @Override
    public void saveGHNShippingInfo(String orderId, String receiverName, String receiverPhone,
                                    Integer toProvinceId, Integer toDistrictId, String toWardCode,
                                    double shippingFee) throws StoreException {
        Connection con = DBUtil.getConnection();
        ensureGhnSchema(con);
        String sql = "UPDATE orders SET receiver_name = ?, receiver_phone = ?, to_province_id = ?, " +
                     "to_district_id = ?, to_ward_code = ?, shipping_fee = ? WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, normalizeOptionalValue(receiverName));
            ps.setString(2, normalizeOptionalValue(receiverPhone));
            if (toProvinceId != null && toProvinceId > 0) ps.setInt(3, toProvinceId);
            else ps.setNull(3, java.sql.Types.INTEGER);
            if (toDistrictId != null && toDistrictId > 0) ps.setInt(4, toDistrictId);
            else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, normalizeOptionalValue(toWardCode));
            ps.setDouble(6, Math.max(0, shippingFee));
            ps.setString(7, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Order getOrderByGhnOrderCode(String ghnOrderCode) throws StoreException {
        if (ghnOrderCode == null || ghnOrderCode.trim().isEmpty()) return null;
        Connection con = DBUtil.getConnection();
        ensureGhnSchema(con);
        String query = "SELECT * FROM orders WHERE ghn_order_code = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, ghnOrderCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderDetailsByOrderId(order.getOrderId(), con));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public String createGHNOrderForSeller(String orderId) throws StoreException {
        Connection con = DBUtil.getConnection();
        ensureGhnSchema(con);
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new StoreException("Đơn hàng không tồn tại");
        }
        if (order.getGhnOrderCode() != null && !order.getGhnOrderCode().trim().isEmpty()) {
            return order.getGhnOrderCode();
        }
        if (!OrderStatus.PENDING.name().equalsIgnoreCase(order.getStatus())
                && !OrderStatus.CONFIRMED.name().equalsIgnoreCase(order.getStatus())) {
            throw new StoreException("Chỉ có thể tạo đơn GHN cho đơn ở trạng thái 'Chờ xác nhận' hoặc 'Đã xác nhận'");
        }
        if (order.getToDistrictId() == null || order.getToDistrictId() <= 0
                || order.getToWardCode() == null || order.getToWardCode().trim().isEmpty()) {
            throw new StoreException("Đơn hàng thiếu mã Quận/Huyện hoặc Phường/Xã GHN");
        }

        GHNClient.CreateOrderRequest req = new GHNClient.CreateOrderRequest();
        req.clientOrderCode = order.getOrderId();
        req.toName = order.getReceiverName() != null && !order.getReceiverName().isEmpty()
                ? order.getReceiverName() : order.getUsername();
        req.toPhone = order.getReceiverPhone() != null && !order.getReceiverPhone().isEmpty()
                ? order.getReceiverPhone() : "0900000000";
        req.toAddress = order.getShippingAddress() != null ? order.getShippingAddress() : "Địa chỉ nhận hàng";
        req.toDistrictId = order.getToDistrictId();
        req.toWardCode = order.getToWardCode().trim();

        boolean isCod = order.getPaymentMethod() != null && order.getPaymentMethod().toUpperCase().contains("COD");
        req.codAmount = isCod ? (int) Math.round(order.getTotalAmount()) : 0;
        req.insuranceValue = (int) Math.round(order.getTotalAmount());
        req.paymentTypeId = 1; // Shop miễn ship -> shop trả phí vận chuyển

        int totalQty = 0;
        List<GHNClient.OrderItemPayload> payloadItems = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderDetail item : order.getItems()) {
                int qty = Math.max(1, item.getQuantity());
                totalQty += qty;
                String name = item.getBookName() != null ? item.getBookName() : item.getBookBarcode();
                int unitPrice = qty > 0 ? (int) Math.round(item.getAmount() / qty) : 0;
                payloadItems.add(new GHNClient.OrderItemPayload(name, qty, 500, unitPrice));
            }
        }
        req.weight = Math.max(500, totalQty * 500);
        req.items = payloadItems;

        GHNClient.CreateOrderResult result;
        try {
            result = GHNClient.createShippingOrder(req);
        } catch (Exception e) {
            throw new StoreException("Không thể tạo vận đơn GHN: " + e.getMessage());
        }

        String sql = "UPDATE orders SET ghn_order_code = ?, ghn_status = 'ready_to_pick', " +
                     "shipping_fee = CASE WHEN ? > 0 THEN ? ELSE shipping_fee END, " +
                     "ghn_updated_at = CURRENT_TIMESTAMP, " +
                     "status = CASE WHEN status = 'PENDING' THEN 'CONFIRMED' ELSE status END " +
                     "WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, result.orderCode);
            ps.setDouble(2, result.totalFee);
            ps.setDouble(3, result.totalFee);
            ps.setString(4, orderId);
            ps.executeUpdate();
            return result.orderCode;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean updateOrderFromGHNWebhook(String ghnOrderCode, String ghnStatus,
                                             String eventTimeIso, String rawPayload) throws StoreException {
        if (ghnOrderCode == null || ghnOrderCode.trim().isEmpty() || ghnStatus == null || ghnStatus.trim().isEmpty()) {
            return false;
        }
        String normalizedGhnCode = ghnOrderCode.trim();
        String normalizedGhnStatus = ghnStatus.trim().toLowerCase();

        Connection con = DBUtil.getConnection();
        ensureGhnSchema(con);

        try (PreparedStatement logPs = con.prepareStatement(
                "INSERT INTO ghn_webhook_logs (ghn_order_code, ghn_status, event_time, raw_payload) VALUES (?, ?, ?, ?)")) {
            logPs.setString(1, normalizedGhnCode);
            logPs.setString(2, normalizedGhnStatus);
            logPs.setString(3, normalizeOptionalValue(eventTimeIso));
            logPs.setString(4, rawPayload);
            logPs.executeUpdate();
        } catch (SQLException ignored) {
        }

        Order order = getOrderByGhnOrderCode(normalizedGhnCode);
        if (order == null) {
            return false;
        }

        OrderStatus currentStatus = OrderStatus.fromString(order.getStatus());
        // Terminal states are immutable
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            return true;
        }

        // Prevent backward progression in normal forward flow
        int currentRank = getGhnProgressRank(order.getGhnStatus());
        int incomingRank = getGhnProgressRank(normalizedGhnStatus);
        if (currentRank >= 0 && incomingRank >= 0 && incomingRank < currentRank) {
            return true;
        }

        OrderStatus mappedStatus = mapGhnStatusToOrderStatus(normalizedGhnStatus, currentStatus);
        boolean shouldRestoreStock = ("returned".equals(normalizedGhnStatus) || "cancel".equals(normalizedGhnStatus))
                && mappedStatus == OrderStatus.CANCELLED;

        try {
            con.setAutoCommit(false);
            if (shouldRestoreStock) {
                String cancelReason = "returned".equals(normalizedGhnStatus)
                        ? "GHN hoàn hàng về shop"
                        : "Vận đơn GHN đã bị hủy";
                String sql = "UPDATE orders SET status = 'CANCELLED', cancelled_by = 'SYSTEM', " +
                             "cancel_reason = ?, cancelled_at = CURRENT_TIMESTAMP, " +
                             "ghn_status = ?, ghn_updated_at = CURRENT_TIMESTAMP " +
                             "WHERE ghn_order_code = ? AND status NOT IN ('COMPLETED', 'CANCELLED')";
                int rows;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, cancelReason);
                    ps.setString(2, normalizedGhnStatus);
                    ps.setString(3, normalizedGhnCode);
                    rows = ps.executeUpdate();
                }
                if (rows > 0 && order.getItems() != null && !order.getItems().isEmpty()) {
                    try (PreparedStatement psStock = con.prepareStatement(
                            "UPDATE books SET quantity = quantity + ? WHERE barcode = ?")) {
                        for (OrderDetail item : order.getItems()) {
                            psStock.setInt(1, item.getQuantity());
                            psStock.setString(2, item.getBookBarcode());
                            psStock.addBatch();
                        }
                        psStock.executeBatch();
                    }
                }
            } else {
                String sql = "UPDATE orders SET status = ?, ghn_status = ?, ghn_updated_at = CURRENT_TIMESTAMP" +
                             (mappedStatus == OrderStatus.SHIPPING ? ", shipped_at = COALESCE(shipped_at, CURRENT_TIMESTAMP)" : "") +
                             " WHERE ghn_order_code = ? AND status NOT IN ('COMPLETED', 'CANCELLED')";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, mappedStatus.name());
                    ps.setString(2, normalizedGhnStatus);
                    ps.setString(3, normalizedGhnCode);
                    ps.executeUpdate();
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            throw new StoreException(ResponseCode.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private int getGhnProgressRank(String ghnStatus) {
        if (ghnStatus == null) return -1;
        switch (ghnStatus.toLowerCase()) {
            case "ready_to_pick": return 1;
            case "picking":
            case "money_collect_picking": return 2;
            case "picked": return 3;
            case "storing":
            case "transporting":
            case "sorting": return 4;
            case "delivering":
            case "money_collect_delivering": return 5;
            case "delivered": return 6;
            default: return -1; // Branching statuses: delivery_fail, return, returned, cancel, exception
        }
    }

    private OrderStatus mapGhnStatusToOrderStatus(String ghnStatus, OrderStatus fallback) {
        switch (ghnStatus) {
            case "ready_to_pick":
            case "picking":
            case "money_collect_picking":
                return OrderStatus.CONFIRMED;
            case "picked":
            case "storing":
            case "transporting":
            case "sorting":
            case "delivering":
            case "money_collect_delivering":
                return OrderStatus.SHIPPING;
            case "delivered":
                return OrderStatus.COMPLETED;
            case "returned":
            case "cancel":
                return OrderStatus.CANCELLED;
            default:
                return fallback;
        }
    }
}
