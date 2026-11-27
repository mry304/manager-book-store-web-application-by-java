<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.PrintWriter, java.net.URLEncoder, java.nio.charset.StandardCharsets, java.text.SimpleDateFormat, java.util.List" %>
<%@ page import="com.bittercode.constant.BookCoinPolicy, com.bittercode.model.ConsignmentStatus, com.bittercode.model.UsedBook, com.bittercode.model.UserRole, com.bittercode.util.StoreUtil" %>
<%
    if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, session)) {
        response.sendRedirect(request.getContextPath() + "/userlog");
        return;
    }
    @SuppressWarnings("unchecked")
    List<UsedBook> consignments = (List<UsedBook>) request.getAttribute("consignments");
    String flashMsg = (String) request.getAttribute("flashMsg");
    String errorMsg = (String) request.getAttribute("errorMsg");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<jsp:include page="CustomerHome.html" />
<%
    PrintWriter tabWriter = new PrintWriter(out);
    StoreUtil.setActiveTab(tabWriter, "my-consignments", session);
    tabWriter.flush();
%>

<main class="bookshelf-canvas" style="padding-top:28px;">
  <div class="bookshelf-page-container" style="max-width:1040px; margin:0 auto;">
    <div class="bookshelf-page-card" style="box-shadow: var(--shadow-canvas);">
      <header class="bookshelf-page-header" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:14px;">
        <div>
          <h1>Yêu Cầu Ký Gửi Của Tôi</h1>
          <p>Theo dõi trạng thái thẩm định sách cũ và Book Coins dự kiến nhận được</p>
        </div>
        <a href="<%= request.getContextPath() %>/consign" class="btn-checkout-shelf">+ Ký gửi sách mới</a>
      </header>

      <% if (flashMsg != null && !flashMsg.isEmpty()) { %>
        <div class="alert alert-success" style="border-radius:12px; margin-bottom:20px;" role="alert">
          <%= StoreUtil.escapeHtml(flashMsg) %>
        </div>
      <% } %>
      <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
        <div class="alert alert-danger" style="border-radius:12px; margin-bottom:20px;" role="alert">
          <%= StoreUtil.escapeHtml(errorMsg) %>
        </div>
      <% } %>

      <% if (consignments == null || consignments.isEmpty()) { %>
        <div class="bookshelf-empty-state">
          <div class="empty-icon">&#128218;</div>
          <h3>Bạn chưa có yêu cầu ký gửi nào</h3>
          <p>Hãy gửi thông tin những cuốn sách cũ còn tốt để bán lại và nhận thưởng Book Coins.</p>
          <a href="<%= request.getContextPath() %>/consign" class="btn-checkout-shelf">Gửi yêu cầu đầu tiên &rarr;</a>
        </div>
      <% } else { %>
        <div style="display:flex; flex-direction:column; gap:16px;">
          <% for (UsedBook ub : consignments) {
              ConsignmentStatus st = ub.getConsignmentStatus();
              if (st == null) st = ConsignmentStatus.PENDING_REVIEW;
              long expectedCoins = ub.getResalePrice() / BookCoinPolicy.CONSIGNMENT_REWARD_DIVISOR;
              String firstImg = (ub.getImageFileNames() != null && !ub.getImageFileNames().isEmpty())
                      ? ub.getImageFileNames().get(0) : null;
          %>
            <article style="display:grid; grid-template-columns:96px 1fr; gap:18px; padding:18px; border:1px solid rgba(197,137,64,0.22); border-radius:16px; background:#fff;">
              <div>
                <% if (firstImg != null) { %>
                  <img src="<%= request.getContextPath() + "/" + BookCoinPolicy.CONSIGNMENT_IMAGE_URL_PREFIX + StoreUtil.escapeHtml(firstImg) %>"
                       alt="<%= StoreUtil.escapeHtml(ub.getTitle()) %>"
                       style="width:96px; height:128px; object-fit:cover; border-radius:10px; border:1px solid #e7dfd3;">
                <% } else { %>
                  <div style="width:96px; height:128px; border-radius:10px; background:#f3ece1; display:flex; align-items:center; justify-content:center; color:#8c7b68; font-size:0.8rem;">
                    Không có ảnh
                  </div>
                <% } %>
              </div>

              <div style="display:flex; flex-direction:column; gap:8px;">
                <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:12px; flex-wrap:wrap;">
                  <div>
                    <h3 style="margin:0; font-size:1.12rem; font-weight:700; color:var(--text-primary);">
                      #<%= ub.getId() %> &mdash; <%= StoreUtil.escapeHtml(ub.getTitle()) %>
                    </h3>
                    <div style="font-size:0.86rem; color:var(--text-secondary); margin-top:2px;">
                      Tác giả: <strong><%= StoreUtil.escapeHtml(ub.getAuthor()) %></strong>
                      &bull; Ngày gửi: <%= ub.getCreatedAt() != null ? sdf.format(ub.getCreatedAt()) : "--" %>
                    </div>
                  </div>
                  <span class="badge-order-base <%= st.getBadgeClass() %>">
                    <%= StoreUtil.escapeHtml(st.getDisplayName()) %>
                  </span>
                </div>

                <div style="display:flex; gap:20px; flex-wrap:wrap; font-size:0.9rem; color:var(--text-primary);">
                  <span>Giá bán lại: <strong style="color:var(--accent-hover);"><%= StoreUtil.formatPrice(ub.getResalePrice()) %></strong></span>
                  <span>Tình trạng: <strong><%= ub.getConditionPercent() %>%</strong></span>
                </div>

                <div style="font-size:0.88rem; color:#15803d; font-weight:600;">
                  Book Coins dự kiến nhận khi đơn hoàn tất: <strong><%= String.format(java.util.Locale.GERMANY, "%,d", expectedCoins) %></strong> Book Coins
                </div>

                <% if ((st == ConsignmentStatus.REJECTED || st == ConsignmentStatus.NEED_MORE_INFO)
                        && ub.getReviewNote() != null && !ub.getReviewNote().trim().isEmpty()) { %>
                  <div style="padding:10px 14px; border-radius:10px; background:<%= st == ConsignmentStatus.REJECTED ? "#fef2f2" : "#fffbeb" %>; border:1px solid <%= st == ConsignmentStatus.REJECTED ? "#fecaca" : "#fde68a" %>; font-size:0.86rem; color:#4b3b2b;">
                    <strong>Ghi chú từ quản trị viên:</strong> <%= StoreUtil.escapeHtml(ub.getReviewNote()) %>
                  </div>
                <% } %>

                <% if (st == ConsignmentStatus.NEED_MORE_INFO) { %>
                  <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; margin-top:4px;">
                    <span style="font-size:0.85rem; color:#b45309; font-weight:600;">
                      Hạn bổ sung: <%= ub.getInfoDeadline() != null ? sdf.format(ub.getInfoDeadline()) : "--" %>
                    </span>
                    <a href="<%= request.getContextPath() %>/consign-respond?id=<%= ub.getId() %>" class="btn-pill-buy">
                      Bổ sung thông tin &rarr;
                    </a>
                  </div>
                <% } %>

                <% if (st == ConsignmentStatus.LISTED && ub.getBookBarcode() != null && !ub.getBookBarcode().trim().isEmpty()) { %>
                  <div style="margin-top:4px;">
                    <a href="<%= request.getContextPath() %>/book-detail?barcode=<%= URLEncoder.encode(ub.getBookBarcode(), StandardCharsets.UTF_8) %>" class="btn-pill-buy">
                      Xem sách trên kệ &rarr;
                    </a>
                  </div>
                <% } %>
              </div>
            </article>
          <% } %>
        </div>
      <% } %>
    </div>
  </div>
</main>
