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
      .rv-main-wrap { padding: 32px 36px 48px; max-width: 1280px; margin: 0 auto; }
      .rv-page-title { font-family: 'Playfair Display', serif; font-size: 1.75rem; font-weight: 800; color: var(--text-primary); margin: 0 0 6px; }
      .rv-page-subtitle { font-size: 0.88rem; color: var(--text-secondary); margin: 0 0 24px; }

      .rv-flash { padding: 12px 20px; border-radius: 12px; font-weight: 600; text-align: center; margin-bottom: 20px; background: #dcfce7; border: 1px solid #bbf7d0; color: #15803d; animation: rvFlashIn .35s ease; }
      @keyframes rvFlashIn { 0% { opacity: 0; transform: translateY(-8px); } 100% { opacity: 1; transform: translateY(0); } }

      /* Filter bar */
      .rv-filter-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 18px 22px; background: #ffffff; border: 1px solid rgba(197,137,64,.15); border-radius: 16px; box-shadow: 0 2px 10px rgba(63,47,31,.05); margin-bottom: 24px; }
      .rv-filter-bar input[type="text"] { flex: 1; min-width: 200px; height:40px; box-sizing:border-box; padding:0 16px; border: 1.5px solid #e2e0db; border-radius: 10px; font-size: 0.9rem; font-family: inherit; outline: none; transition: border-color .2s; color: #333; background: #fff; }
      .rv-filter-bar input[type="text"]:focus { border-color: var(--accent-primary); }
      .rv-filter-bar select { height:40px; box-sizing:border-box; padding:0 14px; border: 1.5px solid #e2e0db; border-radius: 10px; font-size: 0.88rem; font-family: inherit; background: #fff; outline: none; cursor: pointer; color: #333; }
      .rv-filter-bar select:focus { border-color: var(--accent-primary); }


      .rv-filter-reset { display:inline-flex; align-items:center; justify-content:center; height:40px; box-sizing:border-box; padding:0 16px; border: 1.5px solid #e2e0db; border-radius: 10px; background: #fff; color: #666 !important; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none !important; white-space:nowrap; transition: all .2s; }
      .rv-filter-reset:hover { border-color: #b8860b; color: #333 !important; }

      /* Table */
      .rv-table-wrap { background: #ffffff; border: 1px solid rgba(197,137,64,.12); border-radius: 16px; overflow: hidden; box-shadow: 0 2px 10px rgba(63,47,31,.05); }
      .rv-table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
      .rv-table thead th { background: linear-gradient(135deg, #3d2b1f, #4a3728); color: #f4ead4; padding: 14px 16px; text-align: left; font-weight: 700; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.03em; white-space: nowrap; }
      .rv-table tbody tr { border-bottom: 1px solid rgba(197,137,64,.08); transition: background .15s; }
      .rv-table tbody tr:hover { background: rgba(197,137,64,.04); }
      .rv-table tbody tr.rv-row-hidden { opacity: 0.55; background: #fef9f0; }
      .rv-table td { padding: 14px 16px; vertical-align: top; color: var(--text-primary); }

      /* Book info cell */
      .rv-book-cell { display: flex; align-items: center; gap: 10px; min-width: 180px; }
      .rv-book-thumb { width: 42px; height: 56px; border-radius: 6px; object-fit: cover; box-shadow: 0 2px 6px rgba(0,0,0,.12); flex-shrink: 0; }
      .rv-book-thumb-placeholder { width: 42px; height: 56px; border-radius: 6px; background: linear-gradient(135deg, #047857, #0f766e); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 1.1rem; flex-shrink: 0; }
      .rv-book-info { display: flex; flex-direction: column; gap: 2px; }
      .rv-book-name { font-weight: 700; color: var(--text-primary); font-size: 0.85rem; line-height: 1.3; }
      .rv-book-name a { color: var(--text-primary) !important; text-decoration: none !important; }
      .rv-book-name a:hover { color: #b8860b !important; }
      .rv-book-barcode { font-size: 0.75rem; color: var(--text-muted); font-family: monospace; }

      .rv-stars { color: #b8860b; letter-spacing: 1px; font-size: 0.95rem; }
      .rv-comment { color: var(--text-primary); line-height: 1.5; max-width: 260px; word-break: break-word; }

      .rv-reply-box { background: #f0fdf4; border-left: 3px solid #22c55e; padding: 8px 12px; border-radius: 0 8px 8px 0; font-size: 0.82rem; color: #15803d; line-height: 1.5; max-width: 240px; word-break: break-word; }
      .rv-reply-meta { font-size: 0.72rem; color: #64748b; margin-top: 4px; }
      .rv-no-reply { color: var(--text-muted); font-style: italic; font-size: 0.82rem; }

      .rv-badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; }
      .rv-badge-visible { background: #dcfce7; color: #15803d; }
      .rv-badge-hidden { background: #fee2e2; color: #991b1b; }

      /* Action buttons */
      .rv-actions { display:grid; grid-template-columns:1fr; gap:6px; width:146px; min-width:146px; }
      .rv-actions-secondary { display:grid; grid-template-columns:1fr 1fr; gap:6px; }
      .rv-actions-secondary form { min-width:0; margin:0; }
      .rv-btn { display:inline-flex; align-items:center; justify-content:center; gap:5px; box-sizing:border-box; padding:6px 8px; border-radius:8px; font-size:0.76rem; font-weight:700; cursor:pointer; border:none; transition:all .15s; white-space:nowrap; }
      .rv-btn-reply { grid-column:1 / -1; width:100%; height:34px; padding:0 12px; display:inline-flex; align-items:center; justify-content:center; gap:6px; font-size:13px; background:#b8860b !important; color:#ffffff !important; box-shadow:0 2px 8px rgba(184,134,11,.25); }
      .rv-btn-reply svg { width:14px; height:14px; flex-shrink:0; }
      .rv-btn-reply:hover { background: #9a7209 !important; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(184,134,11,.35); }
      .rv-actions-secondary .rv-btn { width:100%; height:32px; gap:4px; padding:0 4px; font-size:.7rem; }
      .rv-actions-secondary .rv-btn svg { width:14px; height:14px; flex:none; }
      .rv-btn-toggle { background: #fef3c7; color: #92400e !important; }
      .rv-btn-toggle:hover { background: #fde68a; }
      .rv-btn-delete { background: #fee2e2; color: #991b1b !important; }
      .rv-btn-delete:hover { background: #fecaca; }

      /* Single reply modal */
      .rv-modal-overlay { position:fixed; inset:0; z-index:9999; display:none; align-items:center; justify-content:center; padding:20px; background:rgba(28,20,13,.55); backdrop-filter:blur(4px); }
      .rv-modal-card { position:relative; z-index:10000; box-sizing:border-box; width:92%; max-width:520px; max-height:calc(100vh - 40px); overflow-y:auto; padding:24px; border-radius:16px; background:#fff; box-shadow:0 20px 50px rgba(0,0,0,.25); animation:rvModalPop .32s cubic-bezier(.34,1.56,.64,1) both; }
      .rv-modal-heading { display:flex; align-items:center; justify-content:space-between; gap:16px; margin-bottom:16px; }
      .rv-modal-heading h2 { margin:0; color:var(--text-primary); font-family:'Playfair Display',serif; font-size:1.25rem; }
      .rv-modal-close { display:grid; place-items:center; width:34px; height:34px; flex:none; border:0; border-radius:50%; background:#f4f1ec; color:#64513a; font-size:1.45rem; line-height:1; cursor:pointer; }
      .rv-modal-close:hover { background:#eee5d7; }
      .rv-modal-quote { margin-bottom:16px; padding:12px 14px; border-left:3px solid #c58f36; border-radius:0 10px 10px 0; background:#faf6ee; color:#554735; font-size:.82rem; line-height:1.5; }
      .rv-modal-quote strong { color:#34291d; }
      .rv-modal-quote p { margin:5px 0 0; white-space:pre-line; overflow-wrap:anywhere; }
      .rv-modal-textarea { display:block; width:100%; min-height:125px; box-sizing:border-box; padding:12px 14px; border:1px solid rgba(197,143,54,.38); border-radius:11px; color:#333; background:#fff; font:inherit; resize:vertical; outline:none; }
      .rv-modal-textarea:focus { border-color:#b8860b; box-shadow:0 0 0 3px rgba(184,134,11,.12); }
      .rv-quick-label { display:block; margin-top:14px; color:#8c7a6b; font-size:12px; font-style:italic; }
      .rv-quick-replies { display:flex; flex-wrap:wrap; gap:8px; margin-top:8px; }
      .rv-quick-chip { appearance:none; -webkit-appearance:none; border:1px solid rgba(197,143,54,.35); border-radius:999px; padding:6px 14px; background:#fdf8ee; color:#6b4c24; font-family:inherit; font-size:12.5px; font-weight:500; line-height:1.3; box-shadow:none; cursor:pointer; transition:all .2s ease; }
      .rv-quick-chip:hover { transform:translateY(-1px); border-color:#b8860b; background:#b8860b; color:#fff; box-shadow:0 5px 12px rgba(184,134,11,.22); }
      .rv-quick-chip:active { transform:scale(.96); }
      .rv-quick-chip:focus-visible { outline:2px solid rgba(184,134,11,.55); outline-offset:2px; }
      .rv-modal-actions { display:flex; justify-content:flex-end; gap:10px; margin-top:16px; }
      .rv-btn-save { min-height:36px; padding:8px 16px; border:0; border-radius:9px; background:#b8860b; color:#fff !important; font-weight:700; font-size:.84rem; cursor:pointer; }
      .rv-btn-save:hover { background:#9a7209; }
      .rv-btn-cancel { min-height:36px; padding:8px 16px; border:1px solid #e3e0db; border-radius:9px; background:#f1f1f1; color:#555 !important; font-weight:600; font-size:.84rem; cursor:pointer; }
      @keyframes rvModalPop { from { opacity:0; transform:translateY(10px) scale(.96); } to { opacity:1; transform:translateY(0) scale(1); } }
      @media(max-width:600px) { .rv-modal-overlay { padding:12px; } .rv-modal-card { width:100%; padding:19px; } }

      .rv-empty { text-align: center; padding: 60px 20px; color: var(--text-secondary); }
      .rv-empty-icon { font-size: 3rem; margin-bottom: 12px; }

      .rv-user-cell { display: flex; flex-direction: column; gap: 2px; }
      .rv-username { font-weight: 700; color: var(--text-primary); font-size: 0.85rem; }
      .rv-date { font-size: 0.75rem; color: var(--text-muted); }

      @media (max-width: 1100px) {
        .rv-main-wrap { padding: 20px 16px; }
        .rv-table-wrap { overflow-x: auto; }
      }
    </style>
    <style>
      .rv-eyebrow { display:inline-flex; align-items:center; gap:7px; margin-bottom:10px; padding:6px 11px; border:1px solid rgba(197,143,54,.28); border-radius:999px; background:rgba(255,255,255,.72); color:#9a6c20; font-size:.7rem; font-weight:800; letter-spacing:.12em; }
      .rv-main-wrap { max-width:1380px; }
      .rv-page-title { margin-top:0; }
      .rv-filter-bar, .rv-table-wrap { background:rgba(255,255,255,.88); backdrop-filter:blur(12px); border:1px solid rgba(197,143,54,.25); box-shadow:0 18px 44px rgba(73,51,26,.08), 0 3px 10px rgba(73,51,26,.04); }
      .rv-filter-bar { animation:rvRise .55s cubic-bezier(.2,.8,.2,1) both; }
      .rv-btn-reply, .rv-btn-save { position:relative; overflow:hidden; background:#b8860b !important; color:#fff !important; }
      .rv-btn-reply::after, .rv-btn-save::after { content:""; position:absolute; inset:-40% auto -40% -70%; width:42%; transform:rotate(18deg); background:linear-gradient(90deg,transparent,rgba(255,255,255,.42),transparent); transition:left .65s cubic-bezier(.2,.8,.2,1); }
      .rv-btn-reply:hover::after, .rv-btn-save:hover::after { left:130%; }
      .rv-btn-reply:active, .rv-btn-save:active { transform:scale(.95); }
      .rv-table-wrap { overflow:auto; }
      .rv-table tbody tr { animation:rvRise .5s cubic-bezier(.2,.8,.2,1) both; transition:transform .28s cubic-bezier(.2,.8,.2,1),background .25s,box-shadow .25s; }
      .rv-table tbody tr:nth-child(1) { animation-delay:45ms; } .rv-table tbody tr:nth-child(2) { animation-delay:90ms; } .rv-table tbody tr:nth-child(3) { animation-delay:135ms; } .rv-table tbody tr:nth-child(4) { animation-delay:180ms; } .rv-table tbody tr:nth-child(5) { animation-delay:225ms; } .rv-table tbody tr:nth-child(n+6) { animation-delay:270ms; }
      .rv-table tbody tr:hover { position:relative; transform:translateY(-2px); background:#fff9ed; box-shadow:inset 4px 0 #c58f36,0 8px 18px rgba(80,55,22,.07); }
      .rv-book-thumb { transition:transform .3s cubic-bezier(.2,.8,.2,1),box-shadow .3s; }
      .rv-table tbody tr:hover .rv-book-thumb { transform:scale(1.07); box-shadow:0 8px 18px rgba(80,55,22,.2); }

      .rv-toast { position:fixed; z-index:1400; top:24px; right:24px; max-width:min(420px,calc(100vw - 32px)); padding:14px 18px; border:1px solid rgba(197,143,54,.32); border-radius:14px; background:rgba(255,255,255,.94); color:#654819; box-shadow:0 18px 45px rgba(50,35,17,.2); backdrop-filter:blur(12px); animation:rvToastIn .45s cubic-bezier(.2,.8,.2,1) both; }
      .rv-toast.is-leaving { animation:rvToastOut .35s ease forwards; }
      @keyframes rvRise { from { opacity:0; transform:translateY(14px); } to { opacity:1; transform:translateY(0); } }
      @keyframes rvToastIn { from { opacity:0; transform:translateX(24px); } to { opacity:1; transform:translateX(0); } }
      @keyframes rvToastOut { to { opacity:0; transform:translateX(24px); } }
      .rv-search-only { display:none; }
      .rv-filter-empty td { padding:24px; text-align:center; color:var(--text-secondary); }
      .rv-table tbody tr.rv-filter-match { animation:rvFilterMatchIn .24s ease both; }
      @keyframes rvFilterMatchIn { from { opacity:0; transform:translateY(5px); } to { opacity:1; transform:translateY(0); } }
      @media(max-width:760px) { .rv-main-wrap { padding:22px 14px 36px; }  .rv-toast { top:12px; right:12px; } }
      @media(prefers-reduced-motion:reduce) { .rv-main-wrap *, .rv-main-wrap *::before, .rv-main-wrap *::after { animation-duration:.01ms !important; transition-duration:.01ms !important; } }
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
<% if (flashMsg != null && !flashMsg.isEmpty()) { %><div class="rv-toast" role="status"><strong>✓</strong> <%= flashMsg %></div><% } %>

<!-- Main Content Area -->
<main class="admin-main-content">
  <div class="rv-main-wrap">

    <div class="rv-eyebrow">✦ TƯƠNG TÁC KHÁCH HÀNG</div>
    <h1 class="rv-page-title">&#11088; Quản lý Đánh giá & Nhận xét</h1>
    <p class="rv-page-subtitle">Xem, phản hồi và quản lý tất cả đánh giá từ khách hàng</p>

    <% if (flashMsg != null && !flashMsg.isEmpty()) { %>
      <div class="rv-flash"><%= flashMsg %></div>
    <% } %>

    <!-- Filter Bar -->
    <form id="rv-filter-form" class="rv-filter-bar" method="get" action="admin-reviews">
      <input id="rv-keyword" type="text" name="keyword" placeholder="&#128269; Tìm theo tên sách, barcode, username, nội dung..."
             value="<%= keyword.replace("\"", "&quot;") %>">
      <select id="rv-rating-filter" name="rating">
        <option value="">Tất cả sao</option>
        <option value="5" <%= "5".equals(ratingFilter) ? "selected" : "" %>>5 sao &#9733;</option>
        <option value="4" <%= "4".equals(ratingFilter) ? "selected" : "" %>>4 sao &#9733;</option>
        <option value="3" <%= "3".equals(ratingFilter) ? "selected" : "" %>>3 sao &#9733;</option>
        <option value="1-2" <%= "1-2".equals(ratingFilter) ? "selected" : "" %>>1-2 sao &#9733;</option>
      </select>
      <select id="rv-reply-filter" name="replyStatus">
        <option value="">Tất cả trạng thái</option>
        <option value="unreplied" <%= "unreplied".equals(replyStatus) ? "selected" : "" %>>Chưa trả lời</option>
        <option value="replied" <%= "replied".equals(replyStatus) ? "selected" : "" %>>Đã trả lời</option>
      </select>
      <a id="rv-filter-reset" href="admin-reviews" class="rv-filter-reset">Xóa bộ lọc</a>
    </form>

    <!-- Reviews Table -->
    <div class="rv-table-wrap">
      <% if (reviews == null || reviews.isEmpty()) { %>
        <div class="rv-empty">
          <div class="rv-empty-icon">&#128172;</div>
          <p style="font-size:1.05rem; font-weight:600;">Không tìm thấy đánh giá nào</p>
          <p style="font-size:0.88rem;">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
        </div>
      <% } else { %>
        <table class="rv-table">
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
            <tr class="<%= isHidden ? "rv-row-hidden" : "" %>" id="row-<%= rv.getId() %>" data-rating="<%= rv.getRating() %>" data-replied="<%= hasReply %>">
              <td>
                <div class="rv-book-cell">
                  <% if (bookImg != null && !bookImg.trim().isEmpty()) { %>
                    <img src="<%= bookImg %>" alt="" class="rv-book-thumb">
                  <% } else { %>
                    <div class="rv-book-thumb-placeholder">&#128218;</div>
                  <% } %>
                  <div class="rv-book-info">
                    <div class="rv-book-name">
                      <a href="book-detail?barcode=<%= rv.getBookBarcode() %>" target="_blank"><%= bookName.length() > 30 ? bookName.substring(0, 28) + "..." : bookName %></a>
                    </div>
                    <span class="rv-search-only rv-full-book-name"><%= bookName %></span>
                    <div class="rv-book-barcode"><%= rv.getBookBarcode() %></div>
                  </div>
                </div>
              </td>
              <td>
                <div class="rv-user-cell">
                  <span class="rv-username"><%= displayName %></span>
                  <span class="rv-search-only"><%= rv.getUsername() %></span>
                  <span class="rv-date"><%= dateStr %></span>
                </div>
              </td>
              <td>
                <div class="rv-stars">
                  <% for (int s = 1; s <= 5; s++) { %><%= s <= rv.getRating() ? "&#9733;" : "&#9734;" %><% } %>
                  <span style="font-size:.78rem; color:#999; margin-left:4px;">(<%= rv.getRating() %>)</span>
                </div>
                <div class="rv-comment" style="margin-top:6px;"><%= rv.getComment() %></div>
              </td>
              <td>
                <% if (hasReply) { %>
                  <div class="rv-reply-box">
                    <%= rv.getAdminReply() %>
                    <div class="rv-reply-meta">
                      &#8212; <%= rv.getRepliedBy() != null ? rv.getRepliedBy() : "Admin" %>
                      <% if (rv.getRepliedAt() != null) { %> | <%= sdf.format(rv.getRepliedAt()) %><% } %>
                    </div>
                  </div>
                <% } else { %>
                  <span class="rv-no-reply">Chưa phản hồi</span>
                <% } %>
              </td>
              <td>
                <span class="rv-badge <%= isHidden ? "rv-badge-hidden" : "rv-badge-visible" %>">
                  <%= isHidden ? "Đã ẩn" : "Hiển thị" %>
                </span>
              </td>
              <td>
                <div class="rv-actions">
                  <span hidden class="rv-admin-reply-source"><%= hasReply ? rv.getAdminReply() : "" %></span>
                  <button type="button" class="rv-btn rv-btn-reply" >
                    <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L8 18l-4 1 1-4Z"/></svg><%= hasReply ? "Sửa phản hồi" : "Trả lời" %>
                  </button>
                  <div class="rv-actions-secondary">
                  <form method="post" action="admin-reviews" style="margin:0;">
                    <input type="hidden" name="action" value="toggle_status">
                    <input type="hidden" name="reviewId" value="<%= rv.getId() %>">
                    <input type="hidden" name="newStatus" value="<%= isHidden ? "VISIBLE" : "HIDDEN" %>">
                    <input type="hidden" name="keyword" value="<%= keyword %>">
                    <input type="hidden" name="rating" value="<%= ratingFilter %>">
                    <input type="hidden" name="replyStatus" value="<%= replyStatus %>">
                    <button type="submit" class="rv-btn rv-btn-toggle"><% if (isHidden) { %><svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>Hiện<% } else { %><svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3l18 18"/><path d="M10.6 10.7a2 2 0 0 0 2.7 2.7"/><path d="M9.9 5.2A11.4 11.4 0 0 1 12 5c6.4 0 10 7 10 7a17 17 0 0 1-3.1 3.8M6.2 6.2C3.5 8 2 12 2 12s3.6 7 10 7c1.3 0 2.5-.3 3.5-.8"/></svg>Ẩn<% } %></button>
                  </form>
                  <form method="post" action="admin-reviews" style="margin:0;" onsubmit="return confirm('Bạn có chắc muốn xóa đánh giá này?');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="reviewId" value="<%= rv.getId() %>">
                    <input type="hidden" name="keyword" value="<%= keyword %>">
                    <input type="hidden" name="rating" value="<%= ratingFilter %>">
                    <input type="hidden" name="replyStatus" value="<%= replyStatus %>">
                    <button type="submit" class="rv-btn rv-btn-delete"><svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="m19 6-1 14H6L5 6"/><path d="M10 11v5M14 11v5"/></svg>Xóa</button>
                  </form>
                  </div>
                </div>
              </td>
            </tr>
            <% } %>
            <tr class="rv-filter-empty" hidden><td colspan="6">Kh&#244;ng t&#236;m th&#7845;y nh&#7853;n x&#233;t n&#224;o ph&#249; h&#7907;p v&#7899;i t&#7915; kh&#243;a c&#7911;a b&#7841;n.</td></tr>
          </tbody>
        </table>
      <% } %>
    </div>
  </div>
</main>

<div id="rv-reply-modal" class="rv-modal-overlay" role="dialog" aria-modal="true" aria-labelledby="rv-modal-title" aria-hidden="true">
  <form id="rv-reply-modal-form" class="rv-modal-card" method="post" action="admin-reviews">
    <input type="hidden" name="action" value="reply">
    <input type="hidden" id="rv-modal-review-id" name="reviewId" value="">
    <input type="hidden" id="rv-modal-keyword" name="keyword" value="">
    <input type="hidden" id="rv-modal-rating" name="rating" value="">
    <input type="hidden" id="rv-modal-reply-status" name="replyStatus" value="">
    <div class="rv-modal-heading">
      <h2 id="rv-modal-title">Phản hồi nhận xét của khách hàng</h2>
      <button type="button" class="rv-modal-close" aria-label="Đóng">×</button>
    </div>
    <div class="rv-modal-quote">
      <div><strong>Sách:</strong> <span id="rv-modal-book"></span></div>
      <div><strong>Khách hàng:</strong> <span id="rv-modal-customer"></span></div>
      <p id="rv-modal-comment"></p>
    </div>
    <textarea id="rv-reply-text" class="rv-modal-textarea" name="replyContent" placeholder="Nhập nội dung phản hồi..." required></textarea>
    <div class="rv-quick-section">
      <span class="rv-quick-label">✦ Gợi ý phản hồi nhanh:</span>
      <div class="rv-quick-replies" aria-label="Câu trả lời gợi ý">
        <button type="button" class="rv-quick-chip" data-reply="Cảm ơn bạn đã tin tưởng và ủng hộ hiệu sách! Chúc bạn có những giờ phút đọc sách thật trọn vẹn và ý nghĩa ạ.">✦ Cảm ơn ủng hộ</button>
        <button type="button" class="rv-quick-chip" data-reply="Cảm ơn góp ý quý báu của bạn! Hiệu sách xin ghi nhận để cải thiện chất lượng dịch vụ tốt hơn mỗi ngày ạ.">✦ Tiếp nhận góp ý</button>
        <button type="button" class="rv-quick-chip" data-reply="Rất vui vì cuốn sách đã mang lại trải nghiệm tốt cho bạn. Hẹn gặp lại bạn ở những tựa sách tiếp theo nhé!">✦ Hẹn gặp lại</button>
      </div>
    </div>
    <div class="rv-modal-actions">
      <button type="button" class="rv-btn-cancel rv-modal-close">Hủy</button>
      <button type="submit" class="rv-btn-save">✓ Lưu phản hồi</button>
    </div>
  </form>
</div>
<script>
var rvReplyModal = document.getElementById("rv-reply-modal");
if (rvReplyModal && rvReplyModal.parentElement !== document.body) document.body.appendChild(rvReplyModal);
var rvReplyModalForm = document.getElementById("rv-reply-modal-form");
function closeReplyModal() {
  if (!rvReplyModal) return;
  rvReplyModal.style.display = "none";
  rvReplyModal.setAttribute("aria-hidden", "true");
  document.body.style.overflow = "";
}
function openReplyModal(button) {
  var row = button && button.closest("tr");
  if (!row || !rvReplyModal || !rvReplyModalForm) return;
  rvReplyModalForm.reset();
  document.getElementById("rv-modal-review-id").value = row.id.replace(/^row-/, "");
  document.getElementById("rv-modal-keyword").value = document.getElementById("rv-keyword").value;
  document.getElementById("rv-modal-rating").value = document.getElementById("rv-rating-filter").value;
  document.getElementById("rv-modal-reply-status").value = document.getElementById("rv-reply-filter").value;
  document.getElementById("rv-modal-book").textContent = (row.querySelector(".rv-full-book-name") || row.querySelector(".rv-book-name") || {}).textContent || "";
  document.getElementById("rv-modal-customer").textContent = (row.querySelector(".rv-username") || {}).textContent || "";
  document.getElementById("rv-modal-comment").textContent = (row.querySelector(".rv-comment") || {}).textContent || "";
  var existingReply = row.querySelector(".rv-admin-reply-source");
  document.getElementById("rv-reply-text").value = existingReply ? existingReply.textContent.trim() : "";
  rvReplyModal.style.display = "flex";
  rvReplyModal.setAttribute("aria-hidden", "false");
  document.body.style.overflow = "hidden";
  document.getElementById("rv-reply-text").focus();
}
document.querySelectorAll(".rv-btn-reply").forEach(function (button) {
  button.addEventListener("click", function () { openReplyModal(button); });
});
document.querySelectorAll(".rv-quick-chip").forEach(function (chip) {
  chip.addEventListener("click", function () {
    var textarea = document.getElementById("rv-reply-text");
    if (textarea) { textarea.value = chip.getAttribute("data-reply"); textarea.focus(); }
  });
});
document.querySelectorAll(".rv-modal-close").forEach(function (button) {
  button.addEventListener("click", closeReplyModal);
});
if (rvReplyModal) {
  rvReplyModal.addEventListener("click", function (event) {
    if (event.target === rvReplyModal) closeReplyModal();
  });
}
document.addEventListener("keydown", function (event) {
  if (event.key === "Escape" && rvReplyModal && rvReplyModal.style.display === "flex") closeReplyModal();
});
var rvFlashMessages = document.querySelectorAll(".rv-flash, .rv-toast");
rvFlashMessages.forEach(function (message) {
  window.setTimeout(function () {
    message.style.transition = "all 0.4s ease";
    message.style.opacity = "0";
    message.style.transform = "translateY(-8px)";
    window.setTimeout(function () { message.style.display = "none"; }, 400);
  }, 3000);
});

(function () {
  var form = document.getElementById("rv-filter-form");
  var keywordInput = document.getElementById("rv-keyword");
  var ratingInput = document.getElementById("rv-rating-filter");
  var replyInput = document.getElementById("rv-reply-filter");
  var resetLink = document.getElementById("rv-filter-reset");
  var tableBody = document.querySelector(".rv-table tbody");
  if (!form || !keywordInput || !ratingInput || !replyInput || !tableBody) return;

  var rows = Array.prototype.slice.call(tableBody.querySelectorAll("tr:not(.rv-filter-empty)"));
  var emptyRow = tableBody.querySelector(".rv-filter-empty");
  function normalize(value) {
    return (value || "").toLocaleLowerCase("vi")
      .replace(new RegExp(String.fromCharCode(273, 272), "g"), "d")
      .normalize("NFD")
      .split("").filter(function (ch) { var code = ch.charCodeAt(0); return code < 768 || code > 879; }).join("");
  }
  function rowSearchText(row) {
    return Array.prototype.map.call(row.querySelectorAll(".rv-book-name, .rv-book-barcode, .rv-username, .rv-search-only, .rv-comment"), function (cell) {
      return cell.textContent || "";
    }).join(" ");
  }
  function applyFilters() {
    var terms = normalize(keywordInput.value).trim().split(" ").filter(Boolean);
    var selectedRating = ratingInput.value;
    var selectedReply = replyInput.value;
    var visibleCount = 0;

    rows.forEach(function (row) {
      var rating = Number(row.getAttribute("data-rating"));
      var replied = row.getAttribute("data-replied") === "true";
      var ratingMatches = !selectedRating ||
        (selectedRating === "1-2" ? rating >= 1 && rating <= 2 : rating === Number(selectedRating));
      var replyMatches = !selectedReply ||
        (selectedReply === "replied" ? replied : !replied);
      var searchableText = normalize(rowSearchText(row));
      var keywordMatches = terms.every(function (term) { return searchableText.indexOf(term) !== -1; });
      var matches = ratingMatches && replyMatches && keywordMatches;

      row.hidden = !matches;
      row.classList.toggle("rv-filter-match", matches);
      if (matches) visibleCount++;
    });
    if (emptyRow) emptyRow.hidden = visibleCount !== 0;
  }

  keywordInput.addEventListener("input", applyFilters);
  ratingInput.addEventListener("change", applyFilters);
  replyInput.addEventListener("change", applyFilters);
  form.addEventListener("submit", function (event) {
    event.preventDefault();
    applyFilters();
  });
  if (resetLink) {
    resetLink.addEventListener("click", function (event) {
      event.preventDefault();
      keywordInput.value = "";
      ratingInput.value = "";
      replyInput.value = "";
      applyFilters();
    });
  }
  applyFilters();
})();

</script>
