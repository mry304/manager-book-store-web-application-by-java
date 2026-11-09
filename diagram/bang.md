**Bảng 1.4. Danh sách tác nhân trong hệ thống**

<table>
  <tr>
    <th>Tác nhân</th>
    <th>Mô tả vai trò</th>
    <th>Các chức năng liên quan</th>
  </tr>
  <tr>
    <td>Khách vãng lai<br>(Guest)</td>
    <td>- Người truy cập ứng dụng nhưng chưa đăng nhập tài khoản.<br>- Tiếp cận các nội dung công khai trước khi trở thành khách hàng của hệ thống.</td>
    <td>Xem danh mục và thông tin sách<br>Sử dụng ở mức giới hạn chức năng hỗ trợ tư vấn<br>Đăng ký tài khoản<br>Đăng nhập hệ thống</td>
  </tr>
  <tr>
    <td>Khách hàng<br>(Customer)</td>
    <td>- Người dùng đã đăng ký và đăng nhập với vai trò khách hàng.<br>- Là tác nhân trung tâm của hoạt động mua sách, tương tác nội dung sách và sử dụng các tiện ích cá nhân.</td>
    <td>Tìm kiếm, xem và lựa chọn sách<br>Quản lý giỏ hàng<br>Đặt hàng và thanh toán<br>Theo dõi hoặc hủy đơn ở trạng thái phù hợp<br>Đánh giá sách<br>Sử dụng trợ lý AI<br>Quản lý kệ sách ảo và thử thách đọc<br>Gửi yêu cầu ký gửi sách cũ</td>
  </tr>
  <tr>
    <td>Quản trị viên/<br>Người bán<br>(Seller/Admin)</td>
    <td>- Người vận hành hoạt động kinh doanh sách.<br>- Kiểm soát các nghiệp vụ quản trị trên hệ thống.</td>
    <td>Quản lý kho sách<br>Xử lý và cập nhật trạng thái đơn hàng<br>Kiểm duyệt sách ký gửi<br>Theo dõi thông báo<br>Xem báo cáo và thống kê phục vụ quản trị</td>
  </tr>
  <tr>
    <td>Hệ thống thanh toán<br>PayOS</td>
    <td>- Dịch vụ thanh toán bên ngoài được tích hợp để hỗ trợ giao dịch trực tuyến bằng VietQR.</td>
    <td>Tiếp nhận yêu cầu tạo thanh toán<br>Cung cấp thông tin thanh toán VietQR<br>Gửi thông báo xác nhận giao dịch về hệ thống</td>
  </tr>
  <tr>
    <td>AI Gateway</td>
    <td>- Dịch vụ AI bên ngoài xử lý truy vấn ngôn ngữ tự nhiên phục vụ người dùng.</td>
    <td>Tiếp nhận yêu cầu tư vấn hoặc tóm tắt sách<br>Xử lý nội dung<br>Trả kết quả để ứng dụng hiển thị cho người dùng</td>
  </tr>
  <tr>
    <td>Mail Server</td>
    <td>- Máy chủ hoặc dịch vụ thư điện tử dùng để gửi thư giao dịch.</td>
    <td>Tiếp nhận yêu cầu gửi thư<br>Chuyển thư xác nhận đơn hàng hoặc thông báo giao dịch tới người nhận</td>
  </tr>
</table>

**Bảng 1.5. Danh sách yêu cầu chức năng của hệ thống**

<table>
  <tr>
    <th>Mã</th>
    <th>Tên</th>
    <th>Mô tả</th>
  </tr>
  <tr><td>FR01</td><td>Đăng ký tài khoản</td><td>- Cho phép khách vãng lai tạo tài khoản khách hàng.<br>- Kiểm tra tính hợp lệ dữ liệu và tránh tạo tài khoản trùng lặp.</td></tr>
  <tr><td>FR02</td><td>Đăng nhập và đăng xuất</td><td>- Cho phép người dùng xác thực để truy cập chức năng theo vai trò.<br>- Thiết lập phiên sau khi đăng nhập và kết thúc phiên khi đăng xuất.</td></tr>
  <tr><td>FR03</td><td>Tìm kiếm, lọc và xem danh mục sách</td><td>- Cho phép xem danh mục sách.<br>- Tìm kiếm theo thông tin sách và lọc theo các tiêu chí được cung cấp.<br>- Hiển thị kết quả theo danh sách có phân trang.</td></tr>
  <tr><td>FR04</td><td>Xem chi tiết và tóm tắt sách</td><td>- Hiển thị nội dung mô tả, giá, số lượng và thông tin đánh giá của đầu sách.<br>- Hỗ trợ yêu cầu tóm tắt ngắn bằng AI.</td></tr>
  <tr><td>FR05</td><td>Đánh giá và nhận xét sách</td><td>- Cho phép khách hàng đã đăng nhập gửi đánh giá theo thang điểm sao và nội dung nhận xét.<br>- Lưu và hiển thị đánh giá liên quan đến đầu sách.</td></tr>
  <tr><td>FR06</td><td>Quản lý giỏ hàng</td><td>- Thêm sách vào giỏ, điều chỉnh số lượng hoặc xóa sách khỏi giỏ.<br>- Kiểm tra số lượng lựa chọn phù hợp với số lượng có thể cung cấp.</td></tr>
  <tr><td>FR07</td><td>Đặt hàng và thanh toán VietQR</td><td>- Tạo đơn hàng từ giỏ hàng và tiếp nhận thông tin giao nhận.<br>- Thanh toán bằng mã VietQR thông qua PayOS.<br>- Ghi nhận đơn hàng và tiếp nhận kết quả thanh toán.</td></tr>
  <tr><td>FR08</td><td>Xác nhận thanh toán và gửi thông báo giao dịch</td><td>- Tiếp nhận thông báo xác nhận từ cổng thanh toán.<br>- Cập nhật trạng thái đơn hàng và số lượng sách liên quan.<br>- Gửi thư điện tử xác nhận giao dịch.</td></tr>
  <tr><td>FR09</td><td>Theo dõi và quản lý đơn hàng của khách hàng</td><td>- Xem lịch sử đơn hàng và tra cứu trạng thái xử lý.<br>- Gửi yêu cầu hủy đơn khi đơn ở trạng thái cho phép hủy.</td></tr>
  <tr><td>FR10</td><td>Trợ lý AI tư vấn sách</td><td>- Nhận câu hỏi hoặc mô tả nhu cầu đọc bằng ngôn ngữ tự nhiên.<br>- Chuyển yêu cầu đến AI Gateway và hiển thị gợi ý sách hoặc phản hồi hỗ trợ.</td></tr>
  <tr><td>FR11</td><td>Ký gửi sách cũ và quản lý Book Coins</td><td>- Gửi yêu cầu ký gửi sách đã qua sử dụng.<br>- Quản lý trạng thái ký gửi, kiểm duyệt và bán lại sách.<br>- Ghi nhận Book Coins theo quy tắc của hệ thống.</td></tr>
  <tr><td>FR12</td><td>Quản lý kệ sách ảo và thử thách đọc</td><td>- Quản lý danh sách sách cá nhân.<br>- Theo dõi tiến độ đọc và thiết lập hoặc theo dõi mục tiêu đọc.</td></tr>
  <tr><td>FR13</td><td>Quản lý kho sách và đơn hàng</td><td>- Thêm, cập nhật hoặc ngừng cung cấp sách.<br>- Theo dõi đơn hàng và cập nhật trạng thái xử lý theo quyền quản trị.</td></tr>
  <tr><td>FR14</td><td>Quản trị sách ký gửi và thông báo</td><td>- Xem, phê duyệt hoặc từ chối yêu cầu ký gửi.<br>- Tiếp nhận và quản lý thông báo về hoạt động cần xử lý.</td></tr>
  <tr><td>FR15</td><td>Báo cáo và thống kê kinh doanh</td><td>- Xem doanh thu, số đơn, số khách hàng, số đầu sách và lượng tồn.<br>- Xem biểu đồ doanh thu theo thời gian và lọc theo mốc thời gian.<br>- Theo dõi sách bán chạy, trạng thái đơn, đơn gần đây, tồn kho thấp và hàng tồn luân chuyển chậm.</td></tr>
</table>