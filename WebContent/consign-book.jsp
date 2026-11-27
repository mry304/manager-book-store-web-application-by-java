<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.PrintWriter, com.bittercode.constant.BookCoinPolicy, com.bittercode.model.UserRole, com.bittercode.util.StoreUtil, com.bittercode.util.UploadConfig" %>
<%
    if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)) {
        response.sendRedirect(request.getContextPath() + "/userlog");
        return;
    }
    String csrfToken = (String) request.getAttribute("csrfToken");
    if (csrfToken == null || csrfToken.isEmpty()) {
        csrfToken = StoreUtil.getOrCreateCsrfToken(session);
    }
    String errorMsg = (String) request.getAttribute("errorMsg");
    String formTitle = (String) request.getAttribute("formTitle");
    String formAuthor = (String) request.getAttribute("formAuthor");
    String formPublisher = (String) request.getAttribute("formPublisher");
    String formDescription = (String) request.getAttribute("formDescription");
    String formOriginalPrice = (String) request.getAttribute("formOriginalPrice");
    String formResalePrice = (String) request.getAttribute("formResalePrice");
    String formConditionPercent = (String) request.getAttribute("formConditionPercent");
%>
<jsp:include page="CustomerHome.html" />
<%
    PrintWriter tabWriter = new PrintWriter(out);
    StoreUtil.setActiveTab(tabWriter, "consign", session);
    tabWriter.flush();
%>

<main class="bookshelf-canvas" style="padding-top:28px;">
  <div class="bookshelf-page-container" style="max-width:780px; margin:0 auto;">
    <div class="bookshelf-page-card" style="box-shadow: var(--shadow-canvas);">
      <header class="bookshelf-page-header" style="text-align:center;">
        <h1>Ký Gửi Sách Cũ</h1>
        <p>Gửi thông tin và hình ảnh sách cũ để cửa hàng thẩm định, đăng bán và tặng thưởng Book Coins</p>
      </header>

      <div style="background:#fffcf5; border:1px solid rgba(197,137,64,0.3); border-radius:12px; padding:14px 18px; margin-bottom:22px; font-size:0.88rem; color:#5a4632;">
        <strong>Chính sách ký gửi:</strong>
        <ul style="margin:6px 0 0; padding-left:18px; line-height:1.55;">
          <li>Điều kiện duyệt tiêu chuẩn: độ mới từ <strong><%= BookCoinPolicy.MIN_APPROVAL_CONDITION_PERCENT %>%</strong> trở lên và giá bán lại không vượt quá giá gốc.</li>
          <li>Khi sách được bán và đơn hàng hoàn tất, bạn nhận thưởng <strong><%= BookCoinPolicy.CONSIGNMENT_REWARD_PERCENT %>%</strong> giá bán lại dưới dạng Book Coins (1 Coin = <%= BookCoinPolicy.COIN_VALUE_VND %> VNĐ).</li>
        </ul>
      </div>

      <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
        <div class="alert alert-danger" style="border-radius:12px; margin-bottom:20px;" role="alert">
          <%= StoreUtil.escapeHtml(errorMsg) %>
        </div>
      <% } %>

      <div id="clientValidationError" class="alert alert-danger" style="display:none; border-radius:12px; margin-bottom:20px;" role="alert"></div>

      <form action="<%= request.getContextPath() %>/consign" method="post" enctype="multipart/form-data" id="consignForm" onsubmit="return validateConsignForm()">
        <input type="hidden" name="csrfToken" value="<%= StoreUtil.escapeHtml(csrfToken) %>">

        <div class="bookshelf-input-group">
          <label for="title">Tiêu đề sách <span style="color:#c2413a;">*</span></label>
          <input type="text" id="title" name="title" maxlength="255" required
                 placeholder="Ví dụ: Nhà Giả Kim"
                 value="<%= StoreUtil.escapeHtml(formTitle) %>">
        </div>

        <div class="bookshelf-input-group">
          <label for="author">Tác giả <span style="color:#c2413a;">*</span></label>
          <input type="text" id="author" name="author" maxlength="100" required
                 placeholder="Ví dụ: Paulo Coelho"
                 value="<%= StoreUtil.escapeHtml(formAuthor) %>">
        </div>

        <div class="bookshelf-input-group">
          <label for="publisher">Nhà xuất bản (tùy chọn)</label>
          <input type="text" id="publisher" name="publisher" maxlength="200"
                 placeholder="Ví dụ: NXB Hội Nhà Văn"
                 value="<%= StoreUtil.escapeHtml(formPublisher) %>">
        </div>

        <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(200px, 1fr)); gap:16px;">
          <div class="bookshelf-input-group">
            <label for="originalPrice">Giá bìa gốc (VNĐ) <span style="color:#c2413a;">*</span></label>
            <input type="number" id="originalPrice" name="originalPrice" min="1" step="1" required
                   placeholder="Ví dụ: 120000"
                   value="<%= StoreUtil.escapeHtml(formOriginalPrice) %>"
                   oninput="updateConsignHints()">
          </div>

          <div class="bookshelf-input-group">
            <label for="resalePrice">Giá bán lại đề xuất (VNĐ) <span style="color:#c2413a;">*</span></label>
            <input type="number" id="resalePrice" name="resalePrice" min="1" step="1" required
                   placeholder="Ví dụ: 75000"
                   value="<%= StoreUtil.escapeHtml(formResalePrice) %>"
                   oninput="updateConsignHints()">
          </div>

          <div class="bookshelf-input-group">
            <label for="conditionPercent">Tình trạng sách (1–100%) <span style="color:#c2413a;">*</span></label>
            <input type="number" id="conditionPercent" name="conditionPercent"
                   min="<%= BookCoinPolicy.MIN_SUBMIT_CONDITION_PERCENT %>"
                   max="<%= BookCoinPolicy.MAX_SUBMIT_CONDITION_PERCENT %>"
                   step="1" required
                   placeholder="Ví dụ: 85"
                   value="<%= StoreUtil.escapeHtml(formConditionPercent) %>"
                   oninput="updateConsignHints()">
          </div>
        </div>

        <div id="rewardEstimateBox" style="margin-bottom:14px; padding:10px 14px; border-radius:10px; background:#f0fdf4; border:1px solid #bbf7d0; color:#166534; font-size:0.88rem;">
          Book Coins dự kiến nhận khi đơn hoàn tất: <strong id="estimatedCoinsValue">0</strong> Book Coins
        </div>

        <div id="approvalHintBox" class="alert alert-warning" style="display:none; border-radius:10px; font-size:0.86rem; margin-bottom:16px;"></div>

        <div class="bookshelf-input-group">
          <label for="description">Mô tả tình trạng chi tiết (tùy chọn)</label>
          <textarea id="description" name="description" rows="4"
                    placeholder="Ghi chú thêm về vết gập, chữ ký, độ ố trang sách..."
                    style="width:100%; padding:12px 16px; border:1px solid #cbd5e1; border-radius:12px; font-family:inherit; font-size:0.95rem; resize:vertical;"><%= StoreUtil.escapeHtml(formDescription) %></textarea>
        </div>

        <div class="bookshelf-input-group">
          <label for="images">Hình ảnh sách thực tế (<%= BookCoinPolicy.MIN_IMAGES %>–<%= BookCoinPolicy.MAX_IMAGES %> ảnh, JPEG/PNG/WEBP, tối đa <%= UploadConfig.MAX_IMAGE_SIZE_BYTES / (1024L * 1024L) %>MB/ảnh) <span style="color:#c2413a;">*</span></label>
          <input type="file" id="images" name="images" multiple required
                 accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                 onchange="validateSelectedImages()"
                 style="display:block; width:100%; padding:10px 14px; background:#fff; border:1px solid #cbd5e1; border-radius:10px;">
          <div id="imagesError" class="text-danger" style="display:none; font-size:0.86rem; font-weight:600; margin-top:6px;" role="alert"></div>
        </div>

        <div style="display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap; margin-top:20px;">
          <a href="<%= request.getContextPath() %>/my-consignments" class="btn-pill-secondary">Xem yêu cầu của tôi</a>
          <button type="submit" class="btn-checkout-shelf">Gửi Yêu Cầu Ký Gửi &rarr;</button>
        </div>
      </form>
    </div>
  </div>
</main>

<script>
var MIN_APPROVAL_COND = <%= BookCoinPolicy.MIN_APPROVAL_CONDITION_PERCENT %>;
var REWARD_DIVISOR = <%= BookCoinPolicy.CONSIGNMENT_REWARD_DIVISOR %>;
var MIN_IMAGES = <%= BookCoinPolicy.MIN_IMAGES %>;
var MAX_IMAGES = <%= BookCoinPolicy.MAX_IMAGES %>;
var MAX_IMAGE_BYTES = <%= UploadConfig.MAX_IMAGE_SIZE_BYTES %>;
var MAX_IMAGE_MB = Math.round(MAX_IMAGE_BYTES / (1024 * 1024));
var ALLOWED_IMAGE_EXT_RE = /\.(jpg|jpeg|png|webp)$/i;

function updateConsignHints() {
  var orig = parseInt(document.getElementById('originalPrice').value, 10);
  var resale = parseInt(document.getElementById('resalePrice').value, 10);
  var cond = parseInt(document.getElementById('conditionPercent').value, 10);

  var coinsEl = document.getElementById('estimatedCoinsValue');
  coinsEl.textContent = (!isNaN(resale) && resale > 0)
    ? Math.floor(resale / REWARD_DIVISOR).toLocaleString('vi-VN')
    : '0';

  var hints = [];
  if (!isNaN(cond) && cond >= 1 && cond < MIN_APPROVAL_COND) {
    hints.push('Gợi ý duyệt: Sách có tình trạng dưới ' + MIN_APPROVAL_COND + '% thường không đủ điều kiện duyệt lên kệ.');
  }
  if (!isNaN(orig) && !isNaN(resale) && orig > 0 && resale > orig) {
    hints.push('Gợi ý duyệt: Giá bán lại đang cao hơn giá gốc; quản trị viên có thể yêu cầu điều chỉnh hoặc từ chối.');
  }

  var hintBox = document.getElementById('approvalHintBox');
  if (hints.length > 0) {
    hintBox.textContent = hints.join(' ');
    hintBox.style.display = 'block';
  } else {
    hintBox.style.display = 'none';
  }
}

function showImagesError(message) {
  var imgErr = document.getElementById('imagesError');
  if (!imgErr) return;
  if (message) {
    imgErr.textContent = message;
    imgErr.style.display = 'block';
  } else {
    imgErr.textContent = '';
    imgErr.style.display = 'none';
  }
}

function validateSelectedImages() {
  var files = document.getElementById('images').files;
  if (!files || files.length < MIN_IMAGES) {
    showImagesError('Vui lòng chọn ít nhất ' + MIN_IMAGES + ' ảnh (tối đa ' + MAX_IMAGES + ' ảnh).');
    return false;
  }
  if (files.length > MAX_IMAGES) {
    showImagesError('Chỉ được chọn tối đa ' + MAX_IMAGES + ' ảnh.');
    return false;
  }
  for (var i = 0; i < files.length; i++) {
    if (!ALLOWED_IMAGE_EXT_RE.test(files[i].name)) {
      showImagesError('Ảnh "' + files[i].name + '" không đúng định dạng (chỉ nhận .jpg, .jpeg, .png, .webp).');
      return false;
    }
    if (files[i].size > MAX_IMAGE_BYTES) {
      showImagesError('Ảnh "' + files[i].name + '" vượt quá ' + MAX_IMAGE_MB + 'MB.');
      return false;
    }
  }
  showImagesError('');
  return true;
}

function validateConsignForm() {
  var errBox = document.getElementById('clientValidationError');
  if (errBox) errBox.style.display = 'none';
  if (!validateSelectedImages()) {
    document.getElementById('images').focus();
    return false;
  }
  return true;
}

document.addEventListener('DOMContentLoaded', updateConsignHints);
</script>
