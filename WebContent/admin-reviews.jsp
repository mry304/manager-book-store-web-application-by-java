<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.bittercode.model.BookReview, java.text.SimpleDateFormat" %>

    <!-- Include Admin Left Sidebar -->
    <jsp:include page="SellerHome.html" />

    <script>
      // Mark "Quản lý đánh giá" tab as active
      if (typeof activeTab !== 'undefined' && document.getElementById(activeTab)) {
        document.getElementById(activeTab).classList.remove('active');
      }
      var reviewTab = document.getElementById('admin-reviews');
      if (reviewTab) {
        reviewTab.classList.add('active');
      }
    </script>

    <style>
      /* ===== Admin Reviews Page Styles ===== */
      .ar-main-wrap { padding: 32px 36px 48px; max-width: 1280px; margin: 0 auto; }
      .ar-page-title { font-family: 'Playfair Display', serif; font-size: 1.75rem; font-weight: 800; color: var(--text-primary); margin: 0 0 6px; }
      .ar-page-subtitle { font-size: 0.88rem; color: var(--text-secondary); margin: 0 0 24px; }

      .ar-flash { padding: 12px 20px; border-radius: 12px; font-weight: 600; text-align: center; margin-bottom: 20px; background: #dcfce7; border: 1px solid #bbf7d0; color: #15803d; animation: arFlashIn .35s ease; }
      @keyframes arFlashIn { 0% { opacity: 0; transform: translateY(-8px); } 100% { opacity: 1; transform: translateY(0); } }

      /* Filter bar */
      .ar-filter-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 18px 22px; background: #ffffff; border: 1px solid rgba(197,137,64,.15); border-radius: 16px; box-shadow: 0 2px 10px rgba(63,47,31,.05); margin-bottom: 24px; }
      .ar-filter-bar input[type="text"] { flex: 1; min-width: 200px; padding: 10px 16px; border: 1.5px solid #e2e0db; border-radius: 10px; font-size: 0.9rem; font-family: inherit; outline: none; transition: border-color .2s; color: #333; background: #fff; }
      .ar-filter-bar input[type="text"]:focus { border-color: var(--accent-primary); }
      .ar-filter-bar select { padding: 10px 14px; border: 1.5px solid #e2e0db; border-radius: 10px; font-size: 0.88rem; font-family: inherit; background: #fff; outline: none; cursor: pointer; color: #333; }
      .ar-filter-bar select:focus { border-color: var(--accent-primary); }

      .ar-filter-btn { padding: 10px 22px; border: none; border-radius: 10px; background: #b8860b; color: #ffffff !important; font-weight: 700; font-size: 0.88rem; cursor: pointer; transition: all .2s; box-shadow: 0 3px 10px rgba(184,134,11,.25); }
      .ar-filter-btn:hover { background: #9a7209; transform: translateY(-1px); box-shadow: 0 5px 16px rgba(184,134,11,.35); }

      .ar-filter-reset { padding: 10px 16px; border: 1.5px solid #e2e0db; border-radius: 10px; background: #fff; color: #666 !important; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none !important; transition: all .2s; }
      .ar-filter-reset:hover { border-color: #b8860b; color: #333 !important; }

      /* Table */
      .ar-table-wrap { background: #ffffff; border: 1px solid rgba(197,137,64,.12); border-radius: 16px; overflow: hidden; box-shadow: 0 2px 10px rgba(63,47,31,.05); }
      .ar-table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
      .ar-table thead th { background: linear-gradient(135deg, #3d2b1f, #4a3728); color: #f4ead4; padding: 14px 16px; text-align: left; font-weight: 700; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.03em; white-space: nowrap; }
      .ar-table tbody tr { border-bottom: 1px solid rgba(197,137,64,.08); transition: background .15s; }
      .ar-table tbody tr:hover { background: rgba(197,137,64,.04); }
      .ar-table tbody tr.ar-row-hidden { opacity: 0.55; background: #fef9f0; }
      .ar-table td { padding: 14px 16px; vertical-align: top; color: var(--text-primary); }

      /* Book info cell */
      .ar-book-cell { display: flex; align-items: center; gap: 10px; min-width: 180px; }
      .ar-book-thumb { width: 42px; height: 56px; border-radius: 6px; object-fit: cover; box-shadow: 0 2px 6px rgba(0,0,0,.12); flex-shrink: 0; }
      .ar-book-thumb-placeholder { width: 42px; height: 56px; border-radius: 6px; background: linear-gradient(135deg, #047857, #0f766e); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 1.1rem; flex-shrink: 0; }
      .ar-book-info { display: flex; flex-direction: column; gap: 2px; }
      .ar-book-name { font-weight: 700; color: var(--text-primary); font-size: 0.85rem; line-height: 1.3; }
      .ar-book-name a { color: var(--text-primary) !important; text-decoration: none !important; }
      .ar-book-name a:hover { color: #b8860b !important; }
      .ar-book-barcode { font-size: 0.75rem; color: var(--text-muted); font-family: monospace; }

      .ar-stars { color: #b8860b; letter-spacing: 1px; font-size: 0.95rem; }
      .ar-comment { color: var(--text-primary); line-height: 1.5; max-width: 260px; word-break: break-word; }

      .ar-reply-box { background: #f0fdf4; border-left: 3px solid #22c55e; padding: 8px 12px; border-radius: 0 8px 8px 0; font-size: 0.82rem; color: #15803d; line-height: 1.5; max-width: 240px; word-break: break-word; }
      .ar-reply-meta { font-size: 0.72rem; color: #64748b; margin-top: 4px; }
      .ar-no-reply { color: var(--text-muted); font-style: italic; font-size: 0.82rem; }

      .ar-badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; }
      .ar-badge-visible { background: #dcfce7; color: #15803d; }
      .ar-badge-hidden { background: #fee2e2; color: #991b1b; }

      /* Action buttons */
      .ar-actions { display: flex; flex-direction: column; gap: 6px; min-width: 120px; }
      .ar-btn { display: inline-flex; align-items: center; gap: 5px; padding: 6px 14px; border-radius: 8px; font-size: 0.78rem; font-weight: 700; cursor: pointer; border: none; transition: all .15s; white-space: nowrap; }
      .ar-btn-reply { background: #b8860b !important; color: #ffffff !important; box-shadow: 0 2px 8px rgba(184,134,11,.25); }
      .ar-btn-reply:hover { background: #9a7209 !important; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(184,134,11,.35); }
      .ar-btn-toggle { background: #fef3c7; color: #92400e !important; }
      .ar-btn-toggle:hover { background: #fde68a; }
      .ar-btn-delete { background: #fee2e2; color: #991b1b !important; }
      .ar-btn-delete:hover { background: #fecaca; }

      /* Inline reply form */
      .ar-inline-reply { display: none; margin-top: 10px; }
      .ar-inline-reply.show { display: block; animation: arSlideIn .25s ease; }
      @keyframes arSlideIn { 0% { opacity: 0; transform: translateY(-6px); } 100% { opacity: 1; transform: translateY(0); } }
      .ar-inline-reply textarea { width: 100%; min-height: 70px; padding: 10px 14px; border: 1.5px solid #e2e0db; border-radius: 10px; font-family: inherit; font-size: 0.85rem; resize: vertical; outline: none; color: #333; background: #fff; }
      .ar-inline-reply textarea:focus { border-color: #b8860b; }
      .ar-inline-reply-actions { display: flex; gap: 8px; margin-top: 8px; }
      .ar-btn-save { padding: 7px 18px; border: none; border-radius: 8px; background: #22c55e; color: #fff !important; font-weight: 700; font-size: 0.82rem; cursor: pointer; }
      .ar-btn-save:hover { background: #16a34a; }
      .ar-btn-cancel { padding: 7px 18px; border: 1.5px solid #e2e0db; border-radius: 8px; background: #fff; color: #666 !important; font-weight: 600; font-size: 0.82rem; cursor: pointer; }

      .ar-empty { text-align: center; padding: 60px 20px; color: var(--text-secondary); }
      .ar-empty-icon { font-size: 3rem; margin-bottom: 12px; }

      .ar-user-cell { display: flex; flex-direction: column; gap: 2px; }
      .ar-username { font-weight: 700; color: var(--text-primary); font-size: 0.85rem; }
      .ar-date { font-size: 0.75rem; color: var(--text-muted); }

      @media (max-width: 1100px) {
        .ar-main-wrap { padding: 20px 16px; }
        .ar-table-wrap { overflow-x: auto; }
      }
    </style>

<%
  List<BookReview> reviews = (List<BookReview>) request.getAttribute("reviews");
  String keyword = (String) request.getAttribute("keyword");
  String ratingFilter = (String) request.getAttribute("ratingFilter");
  String replyStatus = (String) request.getAttribute("replyStatus");
  String flashMsg = (String) request.getAttribute("flashMsg");
  if (keyword == null) keyword = "";
  if (ratingFilter == null) ratingFilter = "";
  if (replyStatus == null) replyStatus = "";
  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>

<!-- Main Content Area -->
<main class="admin-main-content">
  <div class="ar-main-wrap">

    <h1 class="ar-page-title">&#11088; Quản lý Đánh giá & Nhận xét</h1>
    <p class="ar-page-subtitle">Xem, phản hồi và quản lý tất cả đánh giá từ khách hàng</p>

    <% if (flashMsg != null && !flashMsg.isEmpty()) { %>
      <div class="ar-flash"><%= flashMsg %></div>
    <% } %>

    <!-- Filter Bar -->
    <form class="ar-filter-bar" method="get" action="admin-reviews">
      <input type="text" name="keyword" placeholder="&#128269; Tìm theo tên sách, barcode, username, nội dung..."
             value="<%= keyword.replace("\"", "&quot;") %>">
      <select name="rating">
        <option value="">Tất cả sao</option>
        <option value="5" <%= "5".equals(ratingFilter) ? "selected" : "" %>>5 sao &#9733;</option>
        <option value="4" <%= "4".equals(ratingFilter) ? "selected" : "" %>>4 sao &#9733;</option>
        <option value="3" <%= "3".equals(ratingFilter) ? "selected" : "" %>>3 sao &#9733;</option>
        <option value="1-2" <%= "1-2".equals(ratingFilter) ? "selected" : "" %>>1-2 sao &#9733;</option>
      </select>
      <select name="replyStatus">
        <option value="">Tất cả trạng thái</option>
        <option value="unreplied" <%= "unreplied".equals(replyStatus) ? "selected" : "" %>>Chưa trả lời</option>
        <option value="replied" <%= "replied".equals(replyStatus) ? "selected" : "" %>>Đã trả lời</option>
      </select>
      <button type="submit" class="ar-filter-btn">&#128270; Lọc</button>
      <a href="admin-reviews" class="ar-filter-reset">Xóa bộ lọc</a>
    </form>

    <!-- Reviews Table -->
    <div class="ar-table-wrap">
      <% if (reviews == null || reviews.isEmpty()) { %>
        <div class="ar-empty">
          <div class="ar-empty-icon">&#128172;</div>
          <p style="font-size:1.05rem; font-weight:600;">Không tìm thấy đánh giá nào</p>
          <p style="font-size:0.88rem;">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
        </div>
      <% } else { %>
        <table class="ar-table">
          <thead>
            <tr>
              <th>Thông tin sách</th>
              <th>Người đánh giá</th>
              <th>Mức sao & Nội dung</th>
              <th>Phản hồi Admin</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <% for (BookReview rv : reviews) {
                String bookName = rv.getBookName();
                if (bookName == null || bookName.isEmpty()) bookName = rv.getBookBarcode();
                String bookImg = rv.getBookImageUrl();
                String displayName = rv.getUserFullName();
                if (displayName == null || displayName.trim().isEmpty() || "null null".equals(displayName.trim())) {
                    displayName = rv.getUsername();
                }
                String dateStr = rv.getCreatedAt() != null ? sdf.format(rv.getCreatedAt()) : "";
                String rvStatus = rv.getStatus();
                if (rvStatus == null) rvStatus = "VISIBLE";
                boolean isHidden = "HIDDEN".equals(rvStatus);
                boolean hasReply = rv.getAdminReply() != null && !rv.getAdminReply().trim().isEmpty();
            %>
            <tr class="<%= isHidden ? "ar-row-hidden" : "" %>" id="row-<%= rv.getId() %>">
              <td>
                <div class="ar-book-cell">
                  <% if (bookImg != null && !bookImg.trim().isEmpty()) { %>
                    <img src="<%= bookImg %>" alt="" class="ar-book-thumb">
                  <% } else { %>
                    <div class="ar-book-thumb-placeholder">&#128218;</div>
                  <% } %>
                  <div class="ar-book-info">
                    <div class="ar-book-name">
                      <a href="book-detail?barcode=<%= rv.getBookBarcode() %>" target="_blank"><%= bookName.length() > 30 ? bookName.substring(0, 28) + "..." : bookName %></a>
                    </div>
                    <div class="ar-book-barcode"><%= rv.getBookBarcode() %></div>
                  </div>
                </div>
              </td>
              <td>
                <div class="ar-user-cell">
                  <span class="ar-username"><%= displayName %></span>
                  <span class="ar-date"><%= dateStr %></span>
                </div>
              </td>
              <td>
                <div class="ar-stars">
                  <% for (int s = 1; s <= 5; s++) { %><%= s <= rv.getRating() ? "&#9733;" : "&#9734;" %><% } %>
                  <span style="font-size:.78rem; color:#999; margin-left:4px;">(<%= rv.getRating() %>)</span>
                </div>
                <div class="ar-comment" style="margin-top:6px;"><%= rv.getComment() %></div>
              </td>
              <td>
                <% if (hasReply) { %>
                  <div class="ar-reply-box">
                    <%= rv.getAdminReply() %>
                    <div class="ar-reply-meta">
                      &#8212; <%= rv.getRepliedBy() != null ? rv.getRepliedBy() : "Admin" %>
                      <% if (rv.getRepliedAt() != null) { %> | <%= sdf.format(rv.getRepliedAt()) %><% } %>
                    </div>
                  </div>
                <% } else { %>
                  <span class="ar-no-reply">Chưa phản hồi</span>
                <% } %>
              </td>
              <td>
                <span class="ar-badge <%= isHidden ? "ar-badge-hidden" : "ar-badge-visible" %>">
                  <%= isHidden ? "Đã ẩn" : "Hiển thị" %>
                </span>
              </td>
              <td>
                <div class="ar-actions">
                  <button type="button" class="ar-btn ar-btn-reply" onclick="toggleReplyForm(<%= rv.getId() %>)">
                    &#9997; <%= hasReply ? "Sửa phản hồi" : "Trả lời" %>
                  </button>
                  <form method="post" action="admin-reviews" style="margin:0;">
                    <input type="hidden" name="action" value="toggle_status">
                    <input type="hidden" name="reviewId" value="<%= rv.getId() %>">
                    <input type="hidden" name="newStatus" value="<%= isHidden ? "VISIBLE" : "HIDDEN" %>">
                    <input type="hidden" name="keyword" value="<%= keyword %>">
                    <input type="hidden" name="rating" value="<%= ratingFilter %>">
                    <input type="hidden" name="replyStatus" value="<%= replyStatus %>">
                    <button type="submit" class="ar-btn ar-btn-toggle"><%= isHidden ? "&#128065; Hiện" : "&#128064; Ẩn" %></button>
                  </form>
                  <form method="post" action="admin-reviews" style="margin:0;" onsubmit="return confirm('Bạn có chắc muốn xóa đánh giá này?');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="reviewId" value="<%= rv.getId() %>">
                    <input type="hidden" name="keyword" value="<%= keyword %>">
                    <input type="hidden" name="rating" value="<%= ratingFilter %>">
                    <input type="hidden" name="replyStatus" value="<%= replyStatus %>">
                    <button type="submit" class="ar-btn ar-btn-delete">&#128465; Xóa</button>
                  </form>
                </div>
                <!-- Inline Reply Form -->
                <div class="ar-inline-reply" id="reply-form-<%= rv.getId() %>">
                  <form method="post" action="admin-reviews">
                    <input type="hidden" name="action" value="reply">
                    <input type="hidden" name="reviewId" value="<%= rv.getId() %>">
                    <input type="hidden" name="keyword" value="<%= keyword %>">
                    <input type="hidden" name="rating" value="<%= ratingFilter %>">
                    <input type="hidden" name="replyStatus" value="<%= replyStatus %>">
                    <textarea name="replyContent" placeholder="Nhập nội dung phản hồi..." required><%= hasReply ? rv.getAdminReply() : "" %></textarea>
                    <div class="ar-inline-reply-actions">
                      <button type="submit" class="ar-btn-save">&#10003; Lưu phản hồi</button>
                      <button type="button" class="ar-btn-cancel" onclick="toggleReplyForm(<%= rv.getId() %>)">Hủy</button>
                    </div>
                  </form>
                </div>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
      <% } %>
    </div>
  </div>
</main>

<script>
function toggleReplyForm(id) {
  var form = document.getElementById('reply-form-' + id);
  if (form) {
    form.classList.toggle('show');
    if (form.classList.contains('show')) {
      var ta = form.querySelector('textarea');
      if (ta) ta.focus();
      form.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }
}
</script>
