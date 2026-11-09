<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.bittercode.model.Order, com.bittercode.model.OrderDetail, com.bittercode.service.OrderService, com.bittercode.service.impl.OrderServiceImpl" %>
<%@ page import="java.text.NumberFormat, java.text.SimpleDateFormat, java.util.Locale" %>
<%
    String orderCodeParam = request.getParameter("orderCode");
    String orderIdParam = request.getParameter("orderId");
    Order order = null;

    if (orderCodeParam != null && !orderCodeParam.trim().isEmpty()) {
        try {
            long code = Long.parseLong(orderCodeParam.trim());
            OrderService orderService = new OrderServiceImpl();
            order = orderService.getOrderByOrderCode(code);
        } catch (Exception ignored) {}
    }

    if (order == null && orderIdParam != null && !orderIdParam.trim().isEmpty()) {
        try {
            OrderService orderService = new OrderServiceImpl();
            order = orderService.getOrderById(orderIdParam.trim());
        } catch (Exception ignored) {}
    }

    String formattedTotal = "--";
    String orderId = (order != null) ? order.getOrderId() : (orderIdParam != null ? orderIdParam : "ORD-NEW");
    if (order != null) {
        formattedTotal = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(order.getTotalAmount()) + " đ";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt Hàng Thành Công — <%= orderId %></title>
    <link rel="apple-touch-icon" sizes="180x180" href="./favicons/apple-touch-icon.png">
    <link rel="icon" type="image/png" sizes="32x32" href="./favicons/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="./favicons/favicon-16x16.png">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.4.1/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="css/bookshelf-theme.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <style>
        .success-card {
            max-width: 760px;
            margin: 0 auto;
            background: #ffffff;
            border-radius: 24px;
            box-shadow: 0 16px 44px rgba(61, 42, 24, 0.1);
            border: 1px solid rgba(197, 137, 64, 0.25);
            padding: 40px;
            text-align: center;
        }
        .success-checkmark-circle {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            background: #dcfce7;
            color: #15803d;
            font-size: 46px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            animation: bounceIn 0.5s ease;
        }
        @keyframes bounceIn {
            0% { transform: scale(0.6); opacity: 0; }
            80% { transform: scale(1.08); opacity: 1; }
            100% { transform: scale(1); }
        }
        .order-summary-box {
            background: #fffcf8;
            border: 1px solid #ebdccb;
            border-radius: 16px;
            padding: 24px;
            margin: 28px 0;
            text-align: left;
        }
        .summary-line {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px dashed #eedfcb;
            font-size: 0.95rem;
        }
        .summary-line:last-child {
            border-bottom: none;
            padding-bottom: 0;
        }
        .summary-label {
            color: #7c746b;
        }
        .summary-value {
            font-weight: 700;
            color: #2b2825;
            text-align: right;
        }
        .order-status-badge {
            display: inline-block;
            background: #dcfce7;
            color: #15803d;
            font-size: 0.84rem;
            font-weight: 700;
            padding: 4px 12px;
            border-radius: 999px;
        }
    </style>
</head>
<body class="customer-bookshelf-body">
    <header class="bookshelf-header-wrap">
        <nav class="navbar-bookshelf">
            <a class="brand" href="viewbook">BOOKS</a>
            <div class="pill-tabs">
                <a href="viewbook" id="home">Trang chủ</a>
                <a href="viewbook" id="books">Sách có sẵn</a>
                <a href="orders" id="orders">Lịch sử đơn hàng</a>
                <a href="about" id="about">Giới thiệu</a>
            </div>
            <div class="nav-icon-group">
                <a href="cart" id="cart" class="nav-pill-btn" title="Xem giỏ hàng">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path><line x1="3" y1="6" x2="21" y2="6"></line><path d="M16 10a4 4 0 0 1-8 0"></path></svg>
                    <span>Giỏ hàng</span>
                </a>
                <a href="logout" id="logout" class="nav-pill-btn nav-logout-btn" title="Đăng xuất">
                    <span class="nav-avatar"></span>
                    <span>Đăng xuất</span>
                </a>
            </div>
        </nav>
    </header>

    <div class="bookshelf-page-container" style="padding:40px 16px;">
        <div class="success-card">
            <div class="success-checkmark-circle">
                &#10003;
            </div>
            <h1 style="font-family:'Playfair Display',serif; font-size:2rem; font-weight:700; color:#2b2825; margin-bottom:8px;">
                Thanh Toán &amp; Đặt Hàng Thành Công!
            </h1>
            <p style="font-size:1rem; color:#6b6257;">
                Cảm ơn bạn đã tin tưởng mua sắm tại <strong>BOOKS</strong>. Đơn hàng đã được xác nhận và hệ thống đã gửi email xác nhận chi tiết tới hòm thư của bạn.
            </p>

            <div class="order-summary-box">
                <div class="summary-line">
                    <span class="summary-label">Mã đơn hàng:</span>
                    <span class="summary-value" style="color:var(--accent-primary);"><%= orderId %></span>
                </div>
                <div class="summary-line">
                    <span class="summary-label">Trạng thái:</span>
                    <span class="summary-value">
                        <span class="order-status-badge">&#10003; Đã xác nhận &amp; Đang đóng gói</span>
                    </span>
                </div>
                <div class="summary-line">
                    <span class="summary-label">Phương thức thanh toán:</span>
                    <span class="summary-value"><%= order != null && order.getPaymentMethod() != null ? order.getPaymentMethod() : "Mã VietQR (PayOS)" %></span>
                </div>
                <div class="summary-line">
                    <span class="summary-label">Tổng thanh toán:</span>
                    <span class="summary-value" style="font-size:1.15rem; color:#b97831;"><%= formattedTotal %></span>
                </div>
                <% if (order != null && order.getShippingAddress() != null) { %>
                <div class="summary-line">
                    <span class="summary-label">Địa chỉ giao hàng:</span>
                    <span class="summary-value" style="max-width:400px; font-weight:500;"><%= order.getShippingAddress() %></span>
                </div>
                <% } %>
            </div>

            <% if (order != null && order.getItems() != null && !order.getItems().isEmpty()) { %>
            <div style="margin-bottom:32px; text-align:left;">
                <h4 style="font-size:1.05rem; font-weight:700; margin-bottom:12px; color:#2b2825;">Danh sách sách đã mua:</h4>
                <div class="table-responsive">
                    <table class="bookshelf-table" style="font-size:0.9rem;">
                        <thead>
                            <tr>
                                <th>Tựa sách</th>
                                <th style="text-align:center;">Số lượng</th>
                                <th style="text-align:right;">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (OrderDetail item : order.getItems()) { %>
                            <tr>
                                <td><%= item.getBookName() != null ? item.getBookName() : item.getBookBarcode() %></td>
                                <td style="text-align:center;"><strong><%= item.getQuantity() %></strong></td>
                                <td style="text-align:right;"><%= NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(item.getAmount()) %> đ</td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
            <% } %>

            <div style="display:flex; justify-content:center; gap:16px; flex-wrap:wrap; margin-top:20px;">
                <a href="orders" class="btn-checkout-shelf" style="padding:12px 28px;">
                    Xem lịch sử đơn hàng &rarr;
                </a>
                <a href="viewbook" class="btn-pill-secondary" style="padding:12px 28px;">
                    &larr; Tiếp tục khám phá sách
                </a>
            </div>
        </div>
    </div>
</body>
</html>
