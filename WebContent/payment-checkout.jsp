<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.NumberFormat, java.util.Locale" %>
<%
    Long orderCode = (Long) request.getAttribute("orderCode");
    String orderId = (String) request.getAttribute("orderId");
    Double totalAmount = (Double) request.getAttribute("totalAmount");
    String qrCode = (String) request.getAttribute("qrCode");
    String checkoutUrl = (String) request.getAttribute("checkoutUrl");
    String accountNumber = (String) request.getAttribute("accountNumber");
    String accountName = (String) request.getAttribute("accountName");
    String bin = (String) request.getAttribute("bin");
    String description = (String) request.getAttribute("description");

    if (orderCode == null) {
        String codeParam = request.getParameter("orderCode");
        if (codeParam != null && !codeParam.trim().isEmpty()) {
            try { orderCode = Long.parseLong(codeParam.trim()); } catch (Exception ignored) {}
        }
    }
    if (orderId == null && orderCode != null) {
        orderId = "ORD" + orderCode;
    }
    if (totalAmount == null) {
        totalAmount = 0.0;
    }
    String formattedAmount = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(totalAmount) + " đ";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán VietQR — Đơn hàng <%= orderId != null ? orderId : "" %></title>
    <link rel="apple-touch-icon" sizes="180x180" href="./favicons/apple-touch-icon.png">
    <link rel="icon" type="image/png" sizes="32x32" href="./favicons/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="./favicons/favicon-16x16.png">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.4.1/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="css/bookshelf-theme.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <!-- QR Code renderer: renders VietQR/EMV string to canvas image -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
    <style>
        .qr-checkout-grid {
            display: grid;
            grid-template-columns: 1.1fr 1.3fr;
            gap: 36px;
            align-items: start;
        }
        @media (max-width: 900px) {
            .qr-checkout-grid {
                grid-template-columns: 1fr;
                gap: 24px;
            }
        }
        .qr-card-box {
            background: #ffffff;
            border-radius: 20px;
            padding: 28px;
            box-shadow: 0 12px 36px rgba(61, 42, 24, 0.08);
            border: 1px solid rgba(197, 137, 64, 0.25);
            text-align: center;
            position: relative;
        }
        .qr-image-wrapper {
            position: relative;
            display: inline-block;
            padding: 14px;
            background: #fff;
            border-radius: 16px;
            border: 2px dashed rgba(197, 137, 64, 0.4);
            margin: 16px 0;
            box-shadow: 0 4px 18px rgba(0,0,0,0.06);
        }
        .qr-image-wrapper img {
            max-width: 100%;
            width: 270px;
            height: 270px;
            object-fit: contain;
            display: block;
            border-radius: 8px;
        }
        .qr-status-pill {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 6px 16px;
            border-radius: 999px;
            background: #eff6ff;
            color: #1d4ed8;
            font-size: 0.88rem;
            font-weight: 700;
            margin-bottom: 12px;
        }
        .pulse-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #2563eb;
            box-shadow: 0 0 0 0 rgba(37, 99, 235, 0.7);
            animation: pulseWave 1.8s infinite;
        }
        @keyframes pulseWave {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(37, 99, 235, 0.7); }
            70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(37, 99, 235, 0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(37, 99, 235, 0); }
        }
        .countdown-timer {
            font-size: 1.15rem;
            font-weight: 700;
            color: #c2413a;
            margin-top: 10px;
            font-family: monospace;
            letter-spacing: 1px;
        }
        .info-card-box {
            background: #fffcf8;
            border-radius: 20px;
            padding: 28px;
            border: 1px solid rgba(197, 137, 64, 0.25);
            box-shadow: 0 12px 36px rgba(61, 42, 24, 0.08);
        }
        .transfer-field {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 14px;
            background: #ffffff;
            border: 1px solid #f0e6d9;
            border-radius: 12px;
            margin-bottom: 12px;
            transition: all 0.2s ease;
        }
        .transfer-field:hover {
            border-color: var(--accent-primary);
            background: #fffdf9;
        }
        .transfer-label {
            font-size: 0.84rem;
            color: #786f66;
            margin-bottom: 2px;
        }
        .transfer-val {
            font-size: 1rem;
            font-weight: 700;
            color: #2b2825;
            word-break: break-all;
        }
        .transfer-val.highlight {
            color: #b97831;
            font-size: 1.15rem;
        }
        .btn-copy {
            background: #f7f2eb;
            color: #735d46;
            border: 1px solid #e5d8c7;
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 0.82rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            white-space: nowrap;
            margin-left: 8px;
        }
        .btn-copy:hover {
            background: var(--accent-primary);
            color: #fff;
            border-color: var(--accent-primary);
        }
        .btn-copy.copied {
            background: #16a34a;
            color: #fff;
            border-color: #16a34a;
        }
        .step-instruction {
            display: flex;
            gap: 12px;
            align-items: flex-start;
            margin-bottom: 12px;
            font-size: 0.88rem;
            color: #594f45;
            line-height: 1.5;
        }
        .step-number {
            width: 22px;
            height: 22px;
            border-radius: 50%;
            background: var(--accent-primary);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.76rem;
            font-weight: 700;
            flex-shrink: 0;
            margin-top: 1px;
        }
        /* Success Overlay */
        .success-overlay {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(30, 27, 24, 0.85);
            backdrop-filter: blur(8px);
            z-index: 999999;
            align-items: center;
            justify-content: center;
        }
        .success-modal {
            background: #fff;
            border-radius: 24px;
            padding: 40px;
            max-width: 460px;
            width: 90%;
            text-align: center;
            box-shadow: 0 24px 60px rgba(0,0,0,0.3);
            animation: bounceIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }
        @keyframes bounceIn {
            0% { opacity: 0; transform: scale(0.8); }
            100% { opacity: 1; transform: scale(1); }
        }
        .success-icon-wrap {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: #dcfce7;
            color: #16a34a;
            font-size: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
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

    <div class="bookshelf-page-container">
        <div class="bookshelf-page-card">
            <header class="bookshelf-page-header" style="text-align:center;">
                <h1>Thanh Toán Qua Mã VietQR</h1>
                <p>Mở ứng dụng Ngân hàng hoặc Ví điện tử quét mã QR để thanh toán tự động trong vài giây.</p>
            </header>

            <div class="qr-checkout-grid">
                <!-- Column 1: QR Display -->
                <div class="qr-card-box">
                    <div class="qr-status-pill">
                        <span class="pulse-dot"></span>
                        <span>Đang chờ chuyển khoản...</span>
                    </div>
                    <h3 style="font-family:'Playfair Display',serif; font-size:1.4rem; font-weight:700; color:#2b2825; margin-bottom:4px;">
                        Quét mã VietQR
                    </h3>
                    <p style="font-size:0.85rem; color:#786f66; margin-bottom:8px;">
                        Hỗ trợ tất cả ứng dụng Ngân hàng &amp; Ví MoMo
                    </p>

                    <div class="qr-image-wrapper">
                        <div id="qrCodeContainer" style="width:270px;height:270px;display:flex;align-items:center;justify-content:center;"></div>
                    </div>

                    <div style="font-size:0.85rem; color:#786f66;">Mã QR hết hạn sau:</div>
                    <div class="countdown-timer" id="countdownTimer">15:00</div>

                    <% if (checkoutUrl != null && !checkoutUrl.isEmpty()) { %>
                        <div style="margin-top:16px;">
                            <a href="<%= checkoutUrl %>" target="_blank" class="btn-pill-secondary" style="font-size:0.88rem; padding:8px 18px; display:inline-block;">
                                Mở cổng thanh toán PayOS &rarr;
                            </a>
                        </div>
                    <% } %>
                </div>

                <!-- Column 2: Bank Info & Instructions -->
                <div class="info-card-box">
                    <h3 style="font-family:'Playfair Display',serif; font-size:1.25rem; font-weight:700; color:#2b2825; margin-bottom:18px; padding-bottom:8px; border-bottom:1px solid rgba(197,137,64,0.2);">
                        <i class="fa fa-info-circle"></i> Thông tin chuyển khoản thủ công
                    </h3>

                    <div class="transfer-field">
                        <div>
                            <div class="transfer-label">Ngân hàng thụ hưởng</div>
                            <div class="transfer-val"><%= bin != null && !bin.isEmpty() ? bin : "MB Bank (Ngân hàng Quân Đội)" %></div>
                        </div>
                    </div>

                    <div class="transfer-field">
                        <div>
                            <div class="transfer-label">Số tài khoản</div>
                            <div class="transfer-val" id="valAccNum"><%= accountNumber != null && !accountNumber.isEmpty() ? accountNumber : "0888999888" %></div>
                        </div>
                        <button type="button" class="btn-copy" onclick="copyText('valAccNum', this)">Sao chép</button>
                    </div>

                    <div class="transfer-field">
                        <div>
                            <div class="transfer-label">Chủ tài khoản</div>
                            <div class="transfer-val"><%= accountName != null && !accountName.isEmpty() ? accountName : "CONG TY SACH BOOKS" %></div>
                        </div>
                    </div>

                    <div class="transfer-field">
                        <div>
                            <div class="transfer-label">Số tiền cần thanh toán</div>
                            <div class="transfer-val highlight" id="valAmount"><%= (long) (double) totalAmount %></div>
                        </div>
                        <div style="display:flex; align-items:center;">
                            <span style="font-weight:700; color:#b97831; margin-right:8px;"><%= formattedAmount %></span>
                            <button type="button" class="btn-copy" onclick="copyText('valAmount', this)">Sao chép</button>
                        </div>
                    </div>

                    <div class="transfer-field" style="border:1.5px solid #e87a64; background:#fff7f5;">
                        <div>
                            <div class="transfer-label" style="color:#c2413a; font-weight:700;">Nội dung chuyển khoản (Bắt buộc chính xác)</div>
                            <div class="transfer-val" id="valDesc" style="color:#c2413a; font-size:1.15rem;"><%= description != null ? description : ("DH" + orderCode) %></div>
                        </div>
                        <button type="button" class="btn-copy" style="background:#c2413a; color:#fff; border-color:#c2413a;" onclick="copyText('valDesc', this)">Sao chép</button>
                    </div>

                    <div style="margin-top:24px; padding:16px; background:#f5f0e6; border-radius:12px;">
                        <strong style="display:block; font-size:0.9rem; margin-bottom:10px; color:#2b2825;">
                            <i class="fa fa-lightbulb-o"></i> Các bước thanh toán nhanh:
                        </strong>
                        <div class="step-instruction">
                            <span class="step-number">1</span>
                            <span>Mở ứng dụng Ngân hàng của bạn và chọn chức năng <strong>Quét mã QR</strong>.</span>
                        </div>
                        <div class="step-instruction">
                            <span class="step-number">2</span>
                            <span>Quét mã VietQR ở khung bên trái hoặc nhập đúng <strong>Số tài khoản &amp; Nội dung</strong>.</span>
                        </div>
                        <div class="step-instruction">
                            <span class="step-number">3</span>
                            <span>Xác nhận chuyển khoản. Hệ thống sẽ <strong>tự động nhận diện và xác nhận đơn</strong> trong vòng 3 - 5 giây.</span>
                        </div>
                    </div>

                    <div style="margin-top:20px; text-align:center;">
                        <a href="checkout" style="font-size:0.86rem; color:#786f66; text-decoration:underline;">
                            &larr; Hủy thanh toán và quay lại giỏ hàng
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Success Modal Overlay -->
    <div id="successOverlay" class="success-overlay">
        <div class="success-modal">
            <div class="success-icon-wrap">
                &#10003;
            </div>
            <h2 style="font-family:'Playfair Display',serif; font-size:1.6rem; font-weight:700; color:#2b2825; margin-bottom:8px;">
                Thanh Toán Thành Công!
            </h2>
            <p style="font-size:0.95rem; color:#6b6257; margin-bottom:20px;">
                Đơn hàng <strong><%= orderId != null ? orderId : "" %></strong> đã được hệ thống tự động xác nhận và đang chuyển sang đóng gói.
            </p>
            <div style="font-size:0.85rem; color:#9ca3af;">
                Đang chuyển hướng sang trang hoàn tất...
            </div>
        </div>
    </div>

<script>
// ============================================================
// Render QR Code từ chuỗi VietQR/EMV do PayOS trả về
// ============================================================
(function() {
    var qrString = '<%= qrCode != null ? qrCode.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "").replace("\r", "") : "" %>';
    var container = document.getElementById('qrCodeContainer');
    if (!container) return;

    if (qrString && qrString.trim().length > 0) {
        try {
            // Render bằng qrcode.js
            new QRCode(container, {
                text: qrString.trim(),
                width: 260,
                height: 260,
                colorDark: '#2b2825',
                colorLight: '#ffffff',
                correctLevel: QRCode.CorrectLevel.M
            });
            // Center the canvas
            var canvas = container.querySelector('canvas');
            if (canvas) {
                canvas.style.borderRadius = '8px';
                canvas.style.display = 'block';
                canvas.style.margin = '0 auto';
            }
        } catch(e) {
            // Fallback: dùng VietQR API image nếu qrcode.js lỗi
            renderVietQRFallback(container);
        }
    } else {
        // Không có chuỗi QR từ PayOS — dùng VietQR image API
        renderVietQRFallback(container);
    }

    function renderVietQRFallback(container) {
        var bin = '<%= bin != null ? bin : "970422" %>';
        var acc = '<%= accountNumber != null ? accountNumber : "" %>';
        var amt = '<%= (long)(double)totalAmount %>';
        var desc = encodeURIComponent('<%= description != null ? description : "" %>');
        var accName = encodeURIComponent('<%= accountName != null ? accountName : "" %>');
        if (bin && acc) {
            var imgUrl = 'https://img.vietqr.io/image/' + bin + '-' + acc
                + '-compact2.png?amount=' + amt
                + '&addInfo=' + desc
                + '&accountName=' + accName;
            var img = document.createElement('img');
            img.src = imgUrl;
            img.alt = 'Mã VietQR Thanh Toán';
            img.style.cssText = 'width:260px;height:260px;object-fit:contain;border-radius:8px;display:block;margin:0 auto;';
            container.innerHTML = '';
            container.appendChild(img);
        } else {
            container.innerHTML = '<div style="color:#999;font-size:0.9rem;text-align:center;">Không tải được mã QR.<br>Vui lòng nhấn "Mở cổng thanh toán PayOS".</div>';
        }
    }
})();

// Copy text helper
function copyText(elementId, btn) {
    var el = document.getElementById(elementId);
    if (!el) return;
    var text = el.innerText || el.textContent;
    navigator.clipboard.writeText(text.trim()).then(function() {
        var orig = btn.innerText;
        btn.innerText = 'Đã sao chép!';
        btn.classList.add('copied');
        setTimeout(function() {
            btn.innerText = orig;
            btn.classList.remove('copied');
        }, 2000);
    }).catch(function(err) {
        alert('Đã sao chép: ' + text.trim());
    });
}

// 15-minute countdown timer
var totalSeconds = 15 * 60;
var timerEl = document.getElementById('countdownTimer');
var countdownInterval = setInterval(function() {
    totalSeconds--;
    if (totalSeconds <= 0) {
        clearInterval(countdownInterval);
        timerEl.innerText = 'Đã hết hạn';
        timerEl.style.color = '#999';
        return;
    }
    var mins = Math.floor(totalSeconds / 60);
    var secs = totalSeconds % 60;
    timerEl.innerText = (mins < 10 ? '0' : '') + mins + ':' + (secs < 10 ? '0' : '') + secs;
}, 1000);

// Polling check order status every 3 seconds
var orderCode = '<%= orderCode != null ? orderCode : 0 %>';
var isRedirecting = false;

if (orderCode > 0) {
    var pollInterval = setInterval(function() {
        if (isRedirecting) return;
        fetch('api/check-order-status?orderCode=' + orderCode)
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data && (data.status === 'CONFIRMED' || data.isPaid === true)) {
                    clearInterval(pollInterval);
                    clearInterval(countdownInterval);
                    isRedirecting = true;
                    // Show success modal
                    var overlay = document.getElementById('successOverlay');
                    if (overlay) { overlay.style.display = 'flex'; }
                    setTimeout(function() {
                        window.location.href = 'payment-success.jsp?orderCode=' + orderCode;
                    }, 1800);
                }
            })
            .catch(function(err) {
                console.log('[Polling] Check status pending...');
            });
    }, 3000);
}
</script>
</body>
</html>
