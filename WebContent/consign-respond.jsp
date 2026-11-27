<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.PrintWriter, java.text.SimpleDateFormat, com.bittercode.constant.BookCoinPolicy, com.bittercode.model.UsedBook, com.bittercode.model.UserRole, com.bittercode.util.StoreUtil, com.bittercode.util.UploadConfig" %>
<%
    if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)) {
        response.sendRedirect(request.getContextPath() + "/userlog");
        return;
    }
    UsedBook usedBook = (UsedBook) request.getAttribute("usedBook");
    if (usedBook == null) {
        response.sendRedirect(request.getContextPath() + "/my-consignments");
        return;
    }
    String csrfToken = (String) request.getAttribute("csrfToken");
    if (csrfToken == null || csrfToken.isEmpty()) {
        csrfToken = StoreUtil.getOrCreateCsrfToken(session);
    }
    String errorMsg = (String) request.getAttribute("errorMsg");

    String formTitle = request.getAttribute("formTitle") != null
            ? (String) request.getAttribute("formTitle") : usedBook.getTitle();
    String formAuthor = request.getAttribute("formAuthor") != null
            ? (String) request.getAttribute("formAuthor") : usedBook.getAuthor();
    String formPublisher = request.getAttribute("formPublisher") != null
            ? (String) request.getAttribute("formPublisher") : usedBook.getPublisher();
    String formDescription = request.getAttribute("formDescription") != null
            ? (String) request.getAttribute("formDescription") : usedBook.getDescription();
    String formOriginalPrice = request.getAttribute("formOriginalPrice") != null
            ? (String) request.getAttribute("formOriginalPrice") : String.valueOf(usedBook.getOriginalPrice());
    String formResalePrice = request.getAttribute("formResalePrice") != null
            ? (String) request.getAttribute("formResalePrice") : String.valueOf(usedBook.getResalePrice());
    String formConditionPercent = request.getAttribute("formConditionPercent") != null
            ? (String) request.getAttribute("formConditionPercent") : String.valueOf(usedBook.getConditionPercent());

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<jsp:include page="CustomerHome.html" />
<%
    PrintWriter tabWriter = new PrintWriter(out);
    StoreUtil.setActiveTab(tabWriter, "my-consignments", session);
    tabWriter.flush();
%>

<main class="bookshelf-canvas" style="padding-top:28px;">
  <div class="bookshelf-page-container" style="max-width:780px; margin:0 auto;">
    <div class="bookshelf-page-card" style="box-shadow: var(--shadow-canvas);">
      <header class="bookshelf-page-header" style="text-align:center;">
        <h1>Bổ Sung Thông Tin Ký Gửi #<%= usedBook.getId() %></h1>
        <p>Cập nhật thông tin hoặc hình ảnh theo yêu cầu của quản trị viên</p>
      </header>

      <div style="background:#fffbeb; border:1px solid #fde68a; border-radius:12px; padding:14px 18px; margin-bottom:20px; font-size:0.88rem; color:#78350f;">
        <div><strong>Yêu cầu từ quản trị viên:</strong> <%= StoreUtil.escapeHtml(usedBook.getReviewNote()) %></div>
        <div style="margin-top:4px;"><strong>Hạn chót bổ sung:</strong> <%= usedBook.getInfoDeadline() != null ? sdf.format(usedBook.getInfoDeadline()) : "--" %></div>
      </div>

      <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
        <div class="alert alert-danger" style="border-radius:12px; margin-bottom:20px;" role="alert">
          <%= StoreUtil.escapeHtml(errorMsg) %>
        </div>
      <% } %>

      <div id="clientValidationError" class="alert alert-danger" style="display:none; border-radius:12px; margin-bottom:20px;" role="alert"></div>

      <form action="<%= request.getContextPath() %>/consign-respond" method="post" enctype="multipart/form-data" onsubmit="return validateRespondForm()">
        <input type="hidden" name="csrfToken" value="<%= StoreUtil.escapeHtml(csrfToken) %>">
        <input type="hidden" name="id" value="<%= usedBook.getId() %>">

        <div class="bookshelf-input-group">
          <label for="title">Tiêu đề sách <span style="color:#c2413a;">*</span></label>
          <input type="text" id="title" name="title" maxlength="255" required
                 value="<%= StoreUtil.escapeHtml(formTitle) %>">
        </div>

        <div class="bookshelf-input-group">
          <label for="author">Tác giả <span style="color:#c2413a;">*</span></label>
          <input type="text" id="author" name="author" maxlength="100" required
                 value="<%= StoreUtil.escapeHtml(formAuthor) %>">
        </div>

        <div class="bookshelf-input-group">
          <label for="publisher">Nhà xuất bản (tùy chọn)</label>
          <input type="text" id="publisher" name="publisher" maxlength="200"
                 value="<%= StoreUtil.escapeHtml(formPublisher) %>">
        </div>

        <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(200px, 1fr)); gap:16px;">
          <div class="bookshelf-input-group">
            <label for="originalPrice">Giá bìa gốc (VNĐ) <span style="color:#c2413a;">*</span></label>
            <input type="number" id="originalPrice" name="originalPrice" min="1" step="1" required
                   value="<%= StoreUtil.escapeHtml(formOriginalPrice) %>"
                   oninput="updateRespondHints()">
          </div>

          <div class="bookshelf-input-group">
            <label for="resalePrice">Giá bán lại đề xuất (VNĐ) <span style="color:#c2413a;">*</span></label>
            <input type="number" id="resalePrice" name="resalePrice" min="1" step="1" required
                   value="<%= StoreUtil.escapeHtml(formResalePrice) %>"
                   oninput="updateRespondHints()">
          </div>

          <div class="bookshelf-input-group">
            <label for="conditionPercent">Tình trạng sách (1–100%) <span style="color:#c2413a;">*</span></label>
            <input type="number" id="conditionPercent" name="conditionPercent"
                   min="<%= BookCoinPolicy.MIN_SUBMIT_CONDITION_PERCENT %>"
                   max="<%= BookCoinPolicy.MAX_SUBMIT_CONDITION_PERCENT %>"
                   step="1" required
                   value="<%= StoreUtil.escapeHtml(formConditionPercent) %>"
                   oninput="updateRespondHints()">
          </div>
        </div>

        <div id="approvalHintBox" class="alert alert-warning" style="display:none; border-radius:10px; font-size:0.86rem; margin-bottom:16px;"></div>

        <div class="bookshelf-input-group">
          <label for="description">Mô tả tình trạng chi tiết (tùy chọn)</label>
          <textarea id="description" name="description" rows="4"
                    style="width:100%; padding:12px 16px; border:1px solid #cbd5e1; border-radius:12px; font-family:inherit; font-size:0.95rem; resize:vertical;"><%= StoreUtil.escapeHtml(formDescription) %></textarea>
        </div>

        <% if (usedBook.getImageFileNames() != null && !usedBook.getImageFileNames().isEmpty()) { %>
          <div style="margin-bottom:14px;">
            <div style="font-weight:600; font-size:0.88rem; margin-bottom:8px;">Ảnh hiện tại (sẽ giữ nguyên nếu bạn không chọn ảnh mới):</div>
            <div style="display:flex; gap:10px; flex-wrap:wrap;">
              <% for (String img : usedBook.getImageFileNames()) { %>
                <img src="<%= request.getContextPath() + "/" + BookCoinPolicy.CONSIGNMENT_IMAGE_URL_PREFIX + StoreUtil.escapeHtml(img) %>"
                     alt="Ảnh hiện tại"
                     style="width:76px; height:100px; object-fit:cover; border-radius:8px; border:1px solid #e2d8ca;">
              <% } %>
            </div>
          </div>
        <% } %>

        <div class="bookshelf-input-group">
          <label for="images">Thay bằng ảnh mới (tùy chọn, tối đa <%= BookCoinPolicy.MAX_IMAGES %> ảnh, JPEG/PNG/WEBP, &le; <%= UploadConfig.MAX_IMAGE_SIZE_BYTES / (1024L * 1024L) %>MB/ảnh)</label>
          <input type="file" id="images" name="images" multiple
                 accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                 onchange="validateSelectedImages()"
                 style="display:block; width:100%; padding:10px 14px; background:#fff; border:1px solid #cbd5e1; border-radius:10px;">
          <div id="imagesError" class="text-danger" style="display:none; font-size:0.86rem; font-weight:600; margin-top:6px;" role="alert"></div>
        </div>

        <div style="display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap; margin-top:20px;">
          <a href="<%= request.getContextPath() %>/my-consignments" class="btn-pill-secondary">&larr; Quay lại danh sách</a>
          <button type="submit" class="btn-checkout-shelf">Gửi Bổ Sung Thông Tin &rarr;</button>
        </div>
      </form>
    </div>
  </div>
</main>

<script>
var MIN_APPROVAL_COND = <%= BookCoinPolicy.MIN_APPROVAL_CONDITION_PERCENT %>;
var MAX_IMAGES = <%= BookCoinPolicy.MAX_IMAGES %>;
var MAX_IMAGE_BYTES = <%= UploadConfig.MAX_IMAGE_SIZE_BYTES %>;
var MAX_IMAGE_MB = Math.round(MAX_IMAGE_BYTES / (1024 * 1024));
var ALLOWED_IMAGE_EXT_RE = /\.(jpg|jpeg|png|webp)$/i;

function updateRespondHints() {
  var orig = parseInt(document.getElementById('originalPrice').value, 10);
  var resale = parseInt(document.getElementById('resalePrice').value, 10);
  var cond = parseInt(document.getElementById('conditionPercent').value, 10);
  var hints = [];
  if (!isNaN(cond) && cond >= 1 && cond < MIN_APPROVAL_COND) {
    hints.push('Gợi ý duyệt: Sách có tình trạng dưới ' + MIN_APPROVAL_COND + '% thường không đủ điều kiện duyệt lên kệ.');
  }
  if (!isNaN(orig) && !isNaN(resale) && orig > 0 && resale > orig) {
    hints.push('Gợi ý duyệt: Giá bán lại đang cao hơn giá gốc.');
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
  if (!files || files.length === 0) {
    showImagesError('');
    return true;
  }
  if (files.length > MAX_IMAGES) {
    showImagesError('Chỉ được chọn tối đa ' + MAX_IMAGES + ' ảnh mới.');
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

function validateRespondForm() {
  var errBox = document.getElementById('clientValidationError');
  if (errBox) errBox.style.display = 'none';
  if (!validateSelectedImages()) {
    document.getElementById('images').focus();
    return false;
  }
  return true;
}

document.addEventListener('DOMContentLoaded', updateRespondHints);
</script>
