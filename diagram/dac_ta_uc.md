
**Bảng 2.1. Đặc tả Use Case UC06.3 – Tạo và hiển thị mã VietQR**

<table>
  <tr>
    <td>UC ID and<br>Name:</td>
    <td colspan="3"><strong>UC – 6.3: Tạo và hiển thị mã VietQR</strong></td>
  </tr>
  <tr>
    <td>Created By:</td>
    <td>Name</td>
    <td>Date Created:</td>
    <td>Date</td>
  </tr>
  <tr>
    <td>Primary Actor:</td>
    <td>Khách hàng (Customer)</td>
    <td>Secondary Actors:</td>
    <td>Hệ thống thanh toán PayOS</td>
  </tr>
  <tr>
    <td>Trigger:</td>
    <td colspan="3">Khách hàng đã nhập thông tin giao nhận tại trang thanh toán và nhấn nút yêu cầu thanh toán bằng VietQR.</td>
  </tr>
  <tr>
    <td>Description:</td>
    <td colspan="3">Use case mô tả quá trình hệ thống kiểm tra điều kiện đặt hàng, tạo đơn hàng ở trạng thái chờ thanh toán, tạo yêu cầu thanh toán đến PayOS và hiển thị mã VietQR để khách hàng thực hiện chuyển khoản. Trong trường hợp PayOS không trả về mã QR hợp lệ hoặc không khả dụng, hệ thống sử dụng phương án mã VietQR dự phòng theo cấu hình của cửa hàng. Use case kết thúc khi trang thanh toán hiển thị được thông tin cần thiết để khách hàng thực hiện giao dịch; việc xác nhận giao dịch thuộc UC06.4.</td>
  </tr>
  <tr>
    <td>Preconditions:</td>
    <td colspan="3">PRE-1: Khách hàng đã đăng nhập bằng tài khoản Customer.<br>PRE-2: Giỏ hàng có ít nhất một sách và dữ liệu giỏ hàng còn hiệu lực.<br>PRE-3: Khách hàng đã cung cấp họ tên, thư điện tử và địa chỉ nhận hàng hợp lệ.<br>PRE-4: Mỗi sách trong giỏ hàng còn đáp ứng điều kiện số lượng đặt mua theo quy tắc nghiệp vụ.<br>PRE-5: Cấu hình PayOS hợp lệ nếu sử dụng luồng tạo mã QR động.</td>
  </tr>
  <tr>
    <td>Postconditions:</td>
    <td colspan="3">POST-1: Một đơn hàng và các chi tiết đơn hàng được tạo với trạng thái PENDING, có mã orderCode duy nhất.<br>POST-2: Giỏ hàng của khách hàng được xóa sau khi đơn hàng được tạo thành công.<br>POST-3: Trang thanh toán hiển thị mã VietQR động do PayOS trả về hoặc mã VietQR dự phòng, kèm số tiền và nội dung chuyển khoản.<br>POST-4: Tồn kho chưa bị trừ và đơn hàng chưa được xác nhận thanh toán; các cập nhật này chỉ thực hiện ở UC06.4.</td>
  </tr>
  <tr>
    <td>Normal Flow:</td>
    <td colspan="3"><strong>6.3.0: Tạo và hiển thị mã VietQR</strong><br>1. Khách hàng chọn thanh toán từ giỏ hàng và nhập thông tin giao nhận.<br>2. Khách hàng nhấn nút thanh toán bằng VietQR.<br>3. Hệ thống kiểm tra phiên đăng nhập Customer, kiểm tra giỏ hàng và kiểm tra các trường họ tên, email, địa chỉ nhận hàng.<br>4. Hệ thống tính tổng tiền từ các sách và số lượng trong giỏ hàng; tạo thông tin địa chỉ giao nhận đầy đủ.<br>5. Hệ thống sinh orderCode duy nhất, tạo nội dung chuyển khoản và lưu đơn hàng cùng chi tiết đơn hàng ở trạng thái PENDING.<br>6. Hệ thống xóa giỏ hàng của khách hàng sau khi lưu đơn thành công.<br>7. Hệ thống lập yêu cầu thanh toán gồm mã đơn, số tiền, mô tả, thông tin người mua, danh sách sản phẩm, URL trả về và URL hủy thanh toán.<br>8. Hệ thống tạo chữ ký HMAC-SHA256 theo dữ liệu yêu cầu và gửi yêu cầu tạo thanh toán đến PayOS.<br>9. PayOS kiểm tra yêu cầu, tạo giao dịch và trả về mã QR, đường dẫn thanh toán cùng thông tin tài khoản thụ hưởng.<br>10. Hệ thống tiếp nhận phản hồi, chuẩn bị dữ liệu thanh toán và hiển thị trang VietQR cho khách hàng.<br>11. Hệ thống hiển thị số tiền, nội dung chuyển khoản, thông tin tài khoản, mã QR và đường dẫn mở cổng thanh toán PayOS khi có sẵn.<br>12. Giao diện bắt đầu kiểm tra định kỳ trạng thái đơn hàng; khi PayOS xác nhận thanh toán, luồng tiếp tục ở UC06.4.</td>
  </tr>
  <tr>
    <td>Alternative Flows:</td>
    <td colspan="3"><strong>6.3.1: PayOS không trả về mã QR hợp lệ hoặc không sẵn sàng</strong><br>1. Sau bước 8, PayOS không trả về phản hồi thành công, cấu hình PayOS chưa đầy đủ hoặc xảy ra lỗi kết nối.<br>2. Hệ thống tạo mã VietQR dự phòng từ thông tin tài khoản dự phòng đã được cấu hình, với số tiền và nội dung chuyển khoản gắn với orderCode.<br>3. Hệ thống hiển thị trang thanh toán cùng mã VietQR dự phòng và thông tin chuyển khoản cho khách hàng.<br>4. Hệ thống duy trì đơn ở trạng thái PENDING và chờ xử lý xác nhận theo UC06.4.<br><br><strong>6.3.2: Khách hàng mở cổng thanh toán PayOS</strong><br>1. Sau bước 11, khách hàng chọn liên kết mở cổng thanh toán PayOS.<br>2. Hệ thống điều hướng khách hàng đến đường dẫn checkoutUrl do PayOS trả về trong cửa sổ hoặc thẻ trình duyệt mới.<br>3. Khách hàng thực hiện thanh toán trên giao diện PayOS; kết quả vẫn được xử lý theo UC06.4.</td>
  </tr>
  <tr>
    <td>Exceptions:</td>
    <td colspan="3"><strong>6.3.E1: Khách hàng chưa đăng nhập hoặc phiên không hợp lệ</strong><br>1. Hệ thống không tạo đơn hàng.<br>2. Hệ thống chuyển khách hàng đến trang đăng nhập.<br><br><strong>6.3.E2: Giỏ hàng trống hoặc không còn dữ liệu giỏ hàng</strong><br>1. Hệ thống không tạo yêu cầu thanh toán.<br>2. Hệ thống chuyển khách hàng về trang giỏ hàng để chọn sách.<br><br><strong>6.3.E3: Thiếu hoặc sai thông tin giao nhận</strong><br>1. Hệ thống không tạo đơn hàng.<br>2. Hệ thống thông báo khách hàng cần cung cấp đầy đủ họ tên, email và địa chỉ nhận hàng hợp lệ.<br>3. Khách hàng chỉnh sửa thông tin và thực hiện lại từ bước 2 của Normal Flow.<br><br><strong>6.3.E4: Không thể lưu đơn hàng hoặc chi tiết đơn hàng</strong><br>1. Hệ thống không xóa giỏ hàng và không gửi yêu cầu tạo thanh toán.<br>2. Hệ thống thông báo lỗi tạo giao dịch và hướng khách hàng quay lại bước thanh toán.<br><br><strong>6.3.E5: Số tiền không đáp ứng điều kiện của PayOS</strong><br>1. Hệ thống thông báo đơn hàng chưa đủ điều kiện thanh toán trực tuyến.<br>2. Khách hàng quay lại giỏ hàng để điều chỉnh đơn hoặc liên hệ cửa hàng.<br>3. Hệ thống chỉ tạo yêu cầu PayOS khi tổng thanh toán từ 1.000 VNĐ trở lên; đơn có giá trị thấp hơn không được tạo mã thanh toán và khách hàng phải điều chỉnh giỏ hàng hoặc chọn phương thức thanh toán được cửa hàng hỗ trợ.</td>
  </tr>
  <tr>
    <td>Priority:</td>
    <td colspan="3">High</td>
  </tr>
  <tr>
    <td>Business Rules:</td>
    <td colspan="3">BR1. Chỉ khách hàng đã đăng nhập mới được tạo yêu cầu thanh toán.<br>BR2. Giỏ hàng phải có ít nhất một sản phẩm; số lượng mua không được vượt quá số lượng có thể cung cấp.<br>BR3. Họ tên người nhận phải có từ 2 ký tự, email không được để trống và địa chỉ nhận hàng phải có từ 5 ký tự trước khi tạo đơn.<br>BR4. orderCode phải là số nguyên dương, duy nhất và dùng thống nhất trong đơn hàng, yêu cầu thanh toán và nội dung chuyển khoản.<br>BR5. Yêu cầu PayOS gồm tối thiểu: orderCode, amount, description, buyerName, buyerEmail, items, returnUrl, cancelUrl và chữ ký xác thực.<br>BR6. Chữ ký của yêu cầu thanh toán sử dụng HMAC-SHA256 với khóa bí mật cấu hình cho PayOS; khóa bí mật không được hiển thị trên giao diện hoặc ghi vào báo cáo.<br>BR7. Đơn hàng phải được tạo ở trạng thái PENDING trước khi gọi PayOS. Tồn kho chỉ được cập nhật sau khi giao dịch được xác nhận ở UC06.4.<br>BR8. Sau khi đơn được tạo thành công, giỏ hàng của khách hàng được xóa để tránh tạo lặp cùng một đơn từ cùng dữ liệu giỏ hàng.<br>BR9. Khi không tạo được mã QR động, hệ thống sử dụng thông tin tài khoản dự phòng đã cấu hình để tạo mã VietQR dự phòng; mã phải giữ đúng số tiền và nội dung chuyển khoản của đơn hàng.<br>BR10. Mã thanh toán có hiệu lực 15 phút. Tác vụ nền chạy mỗi 5 phút để chuyển đơn PENDING quá hạn sang EXPIRED; hệ thống không trừ tồn kho và khôi phục giỏ hàng nếu khách chưa tạo đơn mới từ cùng dữ liệu.</td>
  </tr>
  <tr>
    <td>Other Information:</td>
    <td colspan="3">- Endpoint thiết kế: POST /create-qr-payment; trang thanh toán hiển thị mã QR và kiểm tra trạng thái đơn theo chu kỳ 3 giây qua GET /api/check-order-status?orderCode=....<br>- PayOS được đặt thời gian chờ kết nối 10 giây và đọc phản hồi 15 giây; quá thời gian này hệ thống thông báo khách hàng thử lại mà không tạo lặp đơn.<br>- Mã thanh toán có hiệu lực 15 phút. Tác vụ nền chạy mỗi 5 phút để chuyển đơn PENDING quá hạn sang EXPIRED và khôi phục giỏ hàng nếu khách chưa tạo đơn mới.<br>- Mã VietQR dự phòng chỉ dùng để hiển thị hướng dẫn thanh toán; đơn chỉ được xác nhận khi có kết quả đối soát hợp lệ theo UC06.4.</td>
  </tr>
</table>

**Bảng 2.2. Đặc tả Use Case UC06.4 – Xác nhận thanh toán và gửi thư giao dịch**

<table>
  <tr>
    <td>UC ID and<br>Name:</td>
    <td colspan="3"><strong>UC – 6.4: Xác nhận thanh toán và gửi thư giao dịch</strong></td>
  </tr>
  <tr>
    <td>Created By:</td>
    <td>Name</td>
    <td>Date Created:</td>
    <td>Date</td>
  </tr>
  <tr>
    <td>Primary Actor:</td>
    <td>Hệ thống thanh toán PayOS</td>
    <td>Secondary Actors:</td>
    <td>Mail Server, Khách hàng (Customer)</td>
  </tr>
  <tr>
    <td>Trigger:</td>
    <td colspan="3">PayOS gửi webhook thông báo giao dịch thanh toán thành công cho đơn hàng có mã orderCode.</td>
  </tr>
  <tr>
    <td>Description:</td>
    <td colspan="3">Use case mô tả quá trình hệ thống tiếp nhận, xác thực và xử lý kết quả thanh toán từ PayOS. Khi giao dịch hợp lệ và thành công, hệ thống cập nhật đơn hàng từ trạng thái PENDING sang CONFIRMED, trừ số lượng tồn của các sách thuộc đơn trong cùng giao dịch dữ liệu, sau đó gửi thư xác nhận giao dịch cho khách hàng. Use case bảo đảm webhook được xử lý an toàn và không làm trừ tồn kho hoặc gửi thư lặp lại khi PayOS gửi lại cùng một thông báo.</td>
  </tr>
  <tr>
    <td>Preconditions:</td>
    <td colspan="3">PRE-1: Đơn hàng đã được tạo từ UC06.2 và có trạng thái PENDING.<br>PRE-2: Đơn hàng có orderCode duy nhất, tổng tiền và danh sách chi tiết đơn hàng hợp lệ.<br>PRE-3: PayOS đã xử lý giao dịch và gửi webhook đến endpoint được cấu hình của hệ thống.<br>PRE-4: Hệ thống có khóa xác thực webhook PayOS hợp lệ trong môi trường vận hành.<br>PRE-5: Thư điện tử nhận thông báo của khách hàng đã được lưu trong thông tin giao nhận hoặc hồ sơ tài khoản.</td>
  </tr>
  <tr>
    <td>Postconditions:</td>
    <td colspan="3">POST-1: Nếu giao dịch thành công và hợp lệ, đơn hàng được chuyển từ PENDING sang CONFIRMED.<br>POST-2: Số lượng tồn của từng sách trong đơn được cập nhật một lần duy nhất, phù hợp với số lượng đã mua.<br>POST-3: Hệ thống xếp yêu cầu gửi thư xác nhận giao dịch cho khách hàng; lỗi gửi thư không làm đảo ngược giao dịch thanh toán đã xác nhận.<br>POST-4: Khi webhook được gửi lại, hệ thống không cập nhật tồn kho hoặc gửi thư lần thứ hai cho cùng đơn hàng.<br>POST-5: Nếu giao dịch không thành công hoặc dữ liệu webhook không hợp lệ, trạng thái đơn hàng và tồn kho không thay đổi.</td>
  </tr>
  <tr>
    <td>Normal Flow:</td>
    <td colspan="3"><strong>6.4.0: Xác nhận thanh toán và gửi thư giao dịch</strong><br>1. PayOS gửi HTTP POST webhook chứa dữ liệu kết quả giao dịch đến hệ thống.<br>2. Hệ thống tiếp nhận payload webhook và kiểm tra payload không rỗng.<br>3. Hệ thống xác thực chữ ký webhook bằng khóa cấu hình PayOS.<br>4. Hệ thống trích xuất orderCode và kiểm tra giao dịch có mã thành công theo quy ước của PayOS.<br>5. Hệ thống truy vấn đơn hàng theo orderCode.<br>6. Hệ thống kiểm tra đơn hàng đang ở trạng thái PENDING.<br>7. Hệ thống mở giao dịch dữ liệu; cập nhật trạng thái đơn từ PENDING sang CONFIRMED.<br>8. Hệ thống kiểm tra và cập nhật số lượng tồn của từng sách trong chi tiết đơn hàng.<br>9. Hệ thống hoàn tất giao dịch dữ liệu khi toàn bộ cập nhật thành công.<br>10. Hệ thống tạo yêu cầu gửi thư xác nhận giao dịch, gồm mã đơn, thông tin sách, số lượng, tổng tiền, phương thức thanh toán và địa chỉ nhận hàng.<br>11. Mail Server gửi thư xác nhận đến địa chỉ thư điện tử của khách hàng.<br>12. Hệ thống trả phản hồi thành công cho PayOS; giao diện của khách hàng nhận được trạng thái CONFIRMED khi kiểm tra trạng thái đơn hàng.</td>
  </tr>
  <tr>
    <td>Alternative Flows:</td>
    <td colspan="3"><strong>6.4.1: Webhook trùng lặp</strong><br>1. PayOS gửi lại webhook của một giao dịch đã được xác nhận hoặc webhook bị gửi trùng do cơ chế thử lại.<br>2. Hệ thống nhận diện đơn hàng đang ở trạng thái CONFIRMED, SHIPPING hoặc COMPLETED.<br>3. Hệ thống trả phản hồi thành công cho PayOS nhưng không trừ tồn kho và không tạo yêu cầu gửi thư mới.<br><br><strong>6.4.2: Không gửi được thư xác nhận</strong><br>1. Sau bước 10, Mail Server không sẵn sàng, cấu hình SMTP không hợp lệ hoặc địa chỉ người nhận không hợp lệ.<br>2. Hệ thống ghi nhận lỗi gửi thư và giữ nguyên kết quả xác nhận thanh toán, trạng thái đơn hàng và số lượng tồn kho.<br>3. Hệ thống đưa thư vào hàng đợi gửi lại tối đa 3 lần theo khoảng thời gian tăng dần; sau 3 lần thất bại, thư được đánh dấu FAILED để Seller/Admin gửi lại thủ công.<br><br><strong>6.4.3: PayOS thông báo giao dịch không thành công hoặc chưa hoàn tất</strong><br>1. Hệ thống nhận webhook nhưng mã kết quả không biểu thị giao dịch thành công.<br>2. Hệ thống không cập nhật trạng thái đơn, không trừ tồn kho và không gửi thư xác nhận thành công.<br>3. Hệ thống trả phản hồi đã tiếp nhận webhook theo quy ước tích hợp, đồng thời duy trì đơn hàng ở trạng thái PENDING để chờ kết quả hợp lệ hoặc xử lý hết hạn.</td>
  </tr>
  <tr>
    <td>Exceptions:</td>
    <td colspan="3"><strong>6.4.E1: Payload webhook rỗng hoặc không đúng định dạng</strong><br>1. Hệ thống không xử lý giao dịch.<br>2. Hệ thống trả phản hồi lỗi yêu cầu không hợp lệ và ghi nhận nhật ký lỗi.<br><br><strong>6.4.E2: Chữ ký webhook không hợp lệ</strong><br>1. Hệ thống từ chối webhook; không truy vấn hoặc cập nhật đơn hàng theo dữ liệu không xác thực.<br>2. Hệ thống ghi nhận sự kiện an toàn để quản trị viên kiểm tra.<br>3. Hệ thống tuyệt đối không sử dụng dữ liệu webhook chưa xác thực làm dữ liệu dự phòng; chỉ dữ liệu đã vượt qua kiểm tra chữ ký mới được đưa vào xử lý.<br><br><strong>6.4.E3: Không tìm thấy đơn hàng theo orderCode</strong><br>1. Hệ thống không cập nhật tồn kho và không gửi thư.<br>2. Hệ thống ghi nhận sự cố đối soát để quản trị viên kiểm tra giao dịch và đơn hàng.<br><br><strong>6.4.E4: Không đủ tồn kho khi xác nhận giao dịch</strong><br>1. Hệ thống hoàn tác toàn bộ cập nhật trong giao dịch dữ liệu.<br>2. Hệ thống không xác nhận đơn tự động, đồng thời tạo cảnh báo để quản trị viên xử lý đơn và chính sách hoàn tiền hoặc thay thế sách cho khách hàng.<br>3. Hệ thống thực hiện lệnh trừ tồn có điều kiện quantity &gt;= số lượng yêu cầu đối với từng đầu sách; nếu bất kỳ lệnh nào không thành công thì toàn bộ giao dịch bị hoàn tác.<br><br><strong>6.4.E5: Lỗi cơ sở dữ liệu khi cập nhật đơn hoặc tồn kho</strong><br>1. Hệ thống hoàn tác toàn bộ thay đổi của giao dịch.<br>2. Hệ thống trả lỗi máy chủ để PayOS có thể thực hiện cơ chế gửi lại webhook theo chính sách tích hợp.<br>3. Hệ thống ghi nhận lỗi để quản trị viên kiểm tra.</td>
  </tr>
  <tr>
    <td>Priority:</td>
    <td colspan="3">High</td>
  </tr>
  <tr>
    <td>Business Rules:</td>
    <td colspan="3">BR1. Chỉ webhook có chữ ký hợp lệ và kết quả thanh toán thành công từ PayOS mới được quyền xác nhận đơn hàng.<br>BR2. orderCode trên webhook phải khớp với một đơn hàng tồn tại trong hệ thống.<br>BR3. Chỉ đơn ở trạng thái PENDING mới được chuyển sang CONFIRMED bởi luồng xác nhận thanh toán.<br>BR4. Cập nhật trạng thái đơn và trừ tồn kho phải được thực hiện trong cùng một giao dịch dữ liệu; khi một bước thất bại, toàn bộ cập nhật phải được hoàn tác.<br>BR5. Số lượng tồn sau cập nhật không được nhỏ hơn 0. Hệ thống cần kiểm tra đủ số lượng trước khi xác nhận đơn.<br>BR6. Webhook phải được xử lý theo nguyên tắc idempotent: cùng một giao dịch chỉ xác nhận đơn, trừ tồn và gửi thư xác nhận một lần.<br>BR7. Thư xác nhận chỉ được gửi sau khi giao dịch dữ liệu xác nhận đơn và cập nhật tồn kho thành công.<br>BR8. Thư xác nhận phải có tối thiểu mã đơn, thời điểm đặt, trạng thái thanh toán, phương thức thanh toán, danh sách sách, số lượng, tổng tiền và địa chỉ nhận hàng.<br>BR9. Lỗi gửi thư không được làm hủy hoặc thay đổi trạng thái thanh toán đã được xác nhận; hệ thống cần lưu nhật ký và có cơ chế gửi lại.<br>BR10. Đơn PENDING không nhận webhook thành công sau 15 phút chuyển sang EXPIRED. Webhook đến sau khi đơn EXPIRED không tự xác nhận đơn mà được ghi nhận để Seller/Admin đối soát và xử lý hoàn tiền nếu cần.</td>
  </tr>
  <tr>
    <td>Other Information:</td>
    <td colspan="3">- Endpoint được thiết kế: POST /api/payos-webhook.<br>- Thành phần xử lý đơn được thiết kế: OrderService.confirmPaymentAndDeductStock(orderCode).<br>- Dịch vụ thư được thiết kế gửi thư trên luồng nền để không chặn phản hồi webhook.<br>- Trạng thái đơn được giao diện kiểm tra định kỳ 3 giây qua endpoint kiểm tra trạng thái đơn.<br>- Cần điều chỉnh nội dung thư xác nhận để phản ánh đúng trạng thái CONFIRMED sau khi thanh toán được xác nhận; không hiển thị trạng thái “chờ xác nhận” nếu đơn đã được xác nhận thanh toán.</td>
  </tr>
</table>

**Bảng 2.3. Đặc tả Use Case UC06.2 – Tạo đơn hàng từ giỏ hàng**

<table>
  <tr>
    <td>UC ID and<br>Name:</td>
    <td colspan="3"><strong>UC – 6.2: Tạo đơn hàng từ giỏ hàng</strong></td>
  </tr>
  <tr>
    <td>Created By:</td>
    <td>Name</td>
    <td>Date Created:</td>
    <td>Date</td>
  </tr>
  <tr>
    <td>Primary Actor:</td>
    <td>Khách hàng (Customer)</td>
    <td>Secondary Actors:</td>
    <td>None</td>
  </tr>
  <tr>
    <td>Trigger:</td>
    <td colspan="3">Khách hàng đã chọn sách trong giỏ hàng, cung cấp thông tin giao nhận hợp lệ và yêu cầu thanh toán.</td>
  </tr>
  <tr>
    <td>Description:</td>
    <td colspan="3">Use case mô tả quá trình chuyển dữ liệu giỏ hàng của khách hàng thành một đơn hàng chính thức. Hệ thống kiểm tra điều kiện tạo đơn, đối chiếu số lượng tồn của từng sách, tính tổng tiền, tạo đơn hàng và các chi tiết đơn hàng ở trạng thái PENDING. Sau khi toàn bộ dữ liệu đơn được lưu thành công, hệ thống xóa giỏ hàng để tránh tạo lặp giao dịch và chuyển sang UC06.3 để tạo mã VietQR. Việc trừ tồn kho chỉ diễn ra sau khi thanh toán được xác nhận ở UC06.4.</td>
  </tr>
  <tr>
    <td>Preconditions:</td>
    <td colspan="3">PRE-1: Khách hàng đã đăng nhập bằng tài khoản Customer.<br>PRE-2: Giỏ hàng có ít nhất một đầu sách với số lượng lớn hơn 0.<br>PRE-3: Mỗi đầu sách trong giỏ vẫn tồn tại và đang được phép bán.<br>PRE-4: Khách hàng đã cung cấp thông tin giao nhận hợp lệ.<br>PRE-5: Khách hàng chưa có thao tác tạo đơn đang được xử lý từ cùng yêu cầu thanh toán.</td>
  </tr>
  <tr>
    <td>Postconditions:</td>
    <td colspan="3">POST-1: Một đơn hàng mới được tạo với mã đơn duy nhất, trạng thái PENDING và tổng tiền được tính từ dữ liệu giỏ hàng tại thời điểm tạo đơn.<br>POST-2: Mỗi sách trong giỏ hàng được ghi thành một dòng chi tiết đơn hàng kèm số lượng và thành tiền.<br>POST-3: Giỏ hàng chỉ được xóa khi đơn hàng và toàn bộ chi tiết đơn hàng đã được lưu thành công.<br>POST-4: Tồn kho chưa bị trừ; việc trừ tồn được thực hiện sau xác nhận thanh toán thành công ở UC06.4.<br>POST-5: Nếu tạo đơn thất bại, giỏ hàng và tồn kho giữ nguyên.</td>
  </tr>
  <tr>
    <td>Normal Flow:</td>
    <td colspan="3"><strong>6.2.0: Tạo đơn hàng từ giỏ hàng</strong><br>1. Khách hàng chọn chức năng thanh toán từ trang giỏ hàng.<br>2. Hệ thống hiển thị biểu mẫu tiếp nhận thông tin giao nhận.<br>3. Khách hàng nhập họ tên, email, địa chỉ và các thông tin giao nhận cần thiết; sau đó xác nhận thanh toán.<br>4. Hệ thống kiểm tra phiên đăng nhập, dữ liệu giao nhận và sự tồn tại của giỏ hàng.<br>5. Hệ thống đọc lại thông tin giá và số lượng tồn hiện tại của từng sách trong giỏ hàng từ nguồn dữ liệu chính thức.<br>6. Hệ thống kiểm tra số lượng yêu cầu của từng sách không vượt quá số lượng có thể cung cấp tại thời điểm tạo đơn.<br>7. Hệ thống tính lại tổng tiền đơn hàng từ giá sách hiện hành và số lượng đã được xác thực.<br>8. Hệ thống sinh mã đơn và orderCode duy nhất; bắt đầu giao dịch dữ liệu.<br>9. Hệ thống tạo bản ghi đơn hàng với trạng thái PENDING, lưu thông tin khách hàng, tổng tiền, địa chỉ giao nhận và phương thức thanh toán VietQR.<br>10. Hệ thống tạo các bản ghi chi tiết đơn hàng tương ứng với từng sách trong giỏ hàng.<br>11. Hệ thống hoàn tất giao dịch dữ liệu khi đơn hàng và toàn bộ chi tiết đơn hàng được lưu thành công.<br>12. Hệ thống xóa giỏ hàng của khách hàng.<br>13. Hệ thống chuyển dữ liệu đơn hàng sang UC06.3 để tạo và hiển thị mã VietQR.</td>
  </tr>
  <tr>
    <td>Alternative Flows:</td>
    <td colspan="3"><strong>6.2.1: Khách hàng điều chỉnh giỏ hàng trước khi xác nhận</strong><br>1. Tại bước 2 hoặc bước 3, khách hàng quay lại giỏ hàng để thêm, giảm số lượng hoặc xóa sách.<br>2. Hệ thống cập nhật giỏ hàng và hiển thị lại tổng tiền tạm tính.<br>3. Khách hàng tiếp tục từ bước 1 của Normal Flow.<br><br><strong>6.2.2: Giá sách thay đổi trước khi tạo đơn</strong><br>1. Tại bước 5, hệ thống phát hiện giá sách hiện hành khác với giá được hiển thị trước đó trong giỏ hàng.<br>2. Hệ thống thông báo các sách có thay đổi giá, cập nhật giỏ hàng theo giá hiện hành và yêu cầu khách hàng xác nhận lại đơn.<br>3. Khách hàng chấp nhận giá mới để tiếp tục từ bước 7 hoặc quay lại giỏ hàng để điều chỉnh.<br><br><strong>6.2.3: Một hoặc nhiều sách không còn đủ tồn kho</strong><br>1. Tại bước 6, hệ thống phát hiện số lượng yêu cầu vượt quá số lượng có thể cung cấp.<br>2. Hệ thống thông báo tên sách, số lượng còn khả dụng và không tạo đơn hàng.<br>3. Khách hàng điều chỉnh giỏ hàng rồi thực hiện lại từ bước 1.</td>
  </tr>
  <tr>
    <td>Exceptions:</td>
    <td colspan="3"><strong>6.2.E1: Khách hàng chưa đăng nhập hoặc phiên đăng nhập không hợp lệ</strong><br>1. Hệ thống không tạo đơn hàng.<br>2. Hệ thống chuyển khách hàng đến trang đăng nhập.<br><br><strong>6.2.E2: Giỏ hàng trống, không tồn tại hoặc chứa sách không còn hợp lệ</strong><br>1. Hệ thống không tạo đơn hàng.<br>2. Hệ thống thông báo khách hàng kiểm tra lại giỏ hàng.<br>3. Hệ thống chuyển khách hàng về trang giỏ hàng.<br><br><strong>6.2.E3: Dữ liệu giao nhận không hợp lệ</strong><br>1. Hệ thống thông báo trường thông tin không hợp lệ hoặc bị bỏ trống.<br>2. Hệ thống giữ nguyên giỏ hàng và yêu cầu khách hàng bổ sung, chỉnh sửa thông tin.<br><br><strong>6.2.E4: Không thể tạo đơn hàng hoặc chi tiết đơn hàng</strong><br>1. Hệ thống hoàn tác toàn bộ giao dịch dữ liệu.<br>2. Hệ thống không xóa giỏ hàng và không chuyển sang UC06.3.<br>3. Hệ thống thông báo lỗi để khách hàng thử lại sau.<br><br><strong>6.2.E5: Yêu cầu tạo đơn bị gửi lặp</strong><br>1. Hệ thống phát hiện yêu cầu trùng lặp khi yêu cầu trước chưa hoàn tất hoặc đã tạo đơn thành công.<br>2. Hệ thống không tạo thêm đơn mới từ cùng một giỏ hàng.<br>3. Hệ thống sử dụng idempotency key do máy chủ phát hành, lưu cùng đơn hàng trong 15 phút và từ chối mọi yêu cầu lặp dùng cùng khóa.</td>
  </tr>
  <tr>
    <td>Priority:</td>
    <td colspan="3">High</td>
  </tr>
  <tr>
    <td>Business Rules:</td>
    <td colspan="3">BR1. Chỉ khách hàng đã đăng nhập mới được tạo đơn hàng từ giỏ hàng.<br>BR2. Đơn hàng phải có ít nhất một chi tiết đơn hàng; mỗi chi tiết phải tham chiếu đến một sách hợp lệ, số lượng lớn hơn 0 và thành tiền không âm.<br>BR3. Hệ thống phải kiểm tra tồn kho lại ngay trước khi tạo đơn, không chỉ dựa vào số lượng hiển thị khi khách thêm sách vào giỏ.<br>BR4. Số lượng đặt mua của từng sách không được vượt quá số lượng có thể cung cấp tại thời điểm tạo đơn. Tồn kho chỉ được trừ sau khi thanh toán thành công.<br>BR5. Tổng tiền đơn hàng phải được tính từ đơn giá hiện hành và số lượng đã kiểm tra; không tin cậy tổng tiền do trình duyệt gửi lên.<br>BR6. Đơn mới được tạo ở trạng thái PENDING và phải có mã đơn/orderCode duy nhất trước khi tạo yêu cầu PayOS.<br>BR7. Tạo đơn hàng và tạo toàn bộ chi tiết đơn hàng phải nằm trong cùng một giao dịch dữ liệu; nếu một chi tiết không thể lưu, toàn bộ đơn phải được hoàn tác.<br>BR8. Giỏ hàng chỉ bị xóa sau khi giao dịch tạo đơn đã thành công. Nếu tạo đơn thất bại, dữ liệu giỏ hàng phải được giữ lại.<br>BR9. Đơn PENDING chỉ là đơn chờ thanh toán; không được ghi nhận là doanh thu hoàn tất hoặc trừ tồn kho trước khi UC06.4 xác nhận thanh toán.<br>BR10. Mỗi lần nhấn đặt hàng dùng một idempotency key duy nhất, có hiệu lực 15 phút. Máy chủ chỉ tạo một đơn cho mỗi khóa; đơn PENDING quá 15 phút chuyển sang EXPIRED và giỏ hàng được khôi phục nếu chưa có đơn mới từ cùng khóa.</td>
  </tr>
  <tr>
    <td>Other Information:</td>
    <td colspan="3">- Thành phần thiết kế tạo đơn trước khi gọi PayOS là OrderService.createOrderWithCode(...).<br>- Đơn, chi tiết đơn và idempotency key được lưu trong cùng một giao dịch dữ liệu; máy chủ kiểm tra tồn kho có điều kiện trước khi tạo đơn.<br>- Đơn tạo thành công mang trạng thái PENDING; giỏ hàng bị xóa sau khi giao dịch được cam kết và được khôi phục nếu đơn EXPIRED.<br>- UC này là tiền đề trực tiếp của UC06.3 và UC06.4.</td>
  </tr>
</table>

**Bảng 2.4. Đặc tả Use Case UC01.2 – Đăng nhập theo vai trò**

<table>
  <tr>
    <td>UC ID and<br>Name:</td>
    <td colspan="3"><strong>UC – 1.2: Đăng nhập theo vai trò</strong></td>
  </tr>
  <tr>
    <td>Created By:</td>
    <td>Name</td>
    <td>Date Created:</td>
    <td>Date</td>
  </tr>
  <tr>
    <td>Primary Actor:</td>
    <td>Khách vãng lai (Guest)</td>
    <td>Secondary Actors:</td>
    <td>Khách hàng (Customer), Quản trị viên/Người bán (Seller/Admin)</td>
  </tr>
  <tr>
    <td>Trigger:</td>
    <td colspan="3">Người dùng truy cập trang đăng nhập của khách hàng hoặc trang đăng nhập quản trị viên/người bán và gửi thông tin xác thực.</td>
  </tr>
  <tr>
    <td>Description:</td>
    <td colspan="3">Use case mô tả quá trình xác thực người dùng theo vai trò Customer hoặc Seller/Admin. Hệ thống tiếp nhận tên đăng nhập hoặc email cùng mật khẩu, kiểm tra thông tin với tài khoản và vai trò tương ứng, sau đó thiết lập phiên làm việc và điều hướng người dùng đến giao diện phù hợp. Sau khi khách hàng đăng nhập thành công, hệ thống khôi phục giỏ hàng cá nhân; sau khi quản trị viên/người bán đăng nhập thành công, hệ thống chuyển đến khu vực thống kê và quản trị.</td>
  </tr>
  <tr>
    <td>Preconditions:</td>
    <td colspan="3">PRE-1: Người dùng chưa có phiên đăng nhập hợp lệ với vai trò đang yêu cầu.<br>PRE-2: Tài khoản đã tồn tại, đang được phép hoạt động và có vai trò Customer hoặc Seller/Admin.<br>PRE-3: Hệ thống có thể truy cập nguồn dữ liệu tài khoản.</td>
  </tr>
  <tr>
    <td>Postconditions:</td>
    <td colspan="3">POST-1: Khi xác thực thành công, hệ thống thiết lập phiên làm việc gắn với đúng vai trò và thông tin định danh người dùng.<br>POST-2: Khách hàng được khôi phục giỏ hàng cá nhân và chuyển đến danh mục sách.<br>POST-3: Quản trị viên/người bán được chuyển đến trang thống kê hoặc khu vực quản trị.<br>POST-4: Khi xác thực thất bại, không tạo phiên đăng nhập có quyền và người dùng nhận thông báo chung phù hợp.</td>
  </tr>
  <tr>
    <td>Normal Flow:</td>
    <td colspan="3"><strong>1.2.0: Đăng nhập theo vai trò</strong><br>1. Người dùng chọn trang đăng nhập dành cho Khách hàng hoặc Quản trị viên/Người bán.<br>2. Hệ thống hiển thị biểu mẫu nhập tên đăng nhập hoặc email và mật khẩu.<br>3. Người dùng nhập thông tin xác thực và gửi yêu cầu đăng nhập.<br>4. Hệ thống kiểm tra các trường bắt buộc và xác định vai trò đăng nhập theo trang hoặc lựa chọn của người dùng.<br>5. Hệ thống truy vấn tài khoản có tên đăng nhập hoặc email tương ứng, mật khẩu hợp lệ và đúng vai trò yêu cầu.<br>6. Hệ thống thiết lập phiên làm việc; lưu định danh người dùng, email và thông tin tài khoản cần thiết trong phiên.<br>7. Nếu vai trò là Customer, hệ thống khôi phục giỏ hàng cá nhân từ dữ liệu đã lưu và điều hướng đến trang danh mục sách.<br>8. Nếu vai trò là Seller/Admin, hệ thống điều hướng đến trang thống kê và quản trị.<br>9. Hệ thống hiển thị giao diện tương ứng với quyền của người dùng.</td>
  </tr>
  <tr>
    <td>Alternative Flows:</td>
    <td colspan="3"><strong>1.2.1: Khách hàng đăng nhập thành công</strong><br>1. Sau bước 6, hệ thống xác định tài khoản có vai trò Customer.<br>2. Hệ thống tải lại giỏ hàng bền vững của khách hàng vào phiên làm việc, nếu có dữ liệu.<br>3. Hệ thống chuyển khách hàng đến trang xem sách.<br><br><strong>1.2.2: Quản trị viên/Người bán đăng nhập thành công</strong><br>1. Sau bước 6, hệ thống xác định tài khoản có vai trò Seller/Admin.<br>2. Hệ thống không khôi phục giỏ hàng cá nhân.<br>3. Hệ thống chuyển người dùng đến trang thống kê và khu vực quản trị.<br><br><strong>1.2.3: Người dùng đăng nhập bằng email</strong><br>1. Ở bước 3, người dùng nhập email thay cho tên đăng nhập.<br>2. Hệ thống đối chiếu email, mật khẩu và vai trò tài khoản.<br>3. Nếu hợp lệ, hệ thống tiếp tục từ bước 6 của Normal Flow.</td>
  </tr>
  <tr>
    <td>Exceptions:</td>
    <td colspan="3"><strong>1.2.E1: Thiếu tên đăng nhập/email hoặc mật khẩu</strong><br>1. Hệ thống không xác thực tài khoản.<br>2. Hệ thống thông báo người dùng nhập đầy đủ các trường bắt buộc.<br><br><strong>1.2.E2: Tên đăng nhập/email hoặc mật khẩu không chính xác</strong><br>1. Hệ thống không tạo phiên đăng nhập.<br>2. Hệ thống hiển thị thông báo chung “Tên đăng nhập hoặc mật khẩu không chính xác” và không tiết lộ trường nào sai.<br>3. Người dùng có thể nhập lại thông tin.<br><br><strong>1.2.E3: Tài khoản tồn tại nhưng không thuộc vai trò đăng nhập yêu cầu</strong><br>1. Hệ thống không tạo phiên có quyền.<br>2. Hệ thống hiển thị thông báo xác thực chung và không tiết lộ vai trò thực tế của tài khoản.<br>3. Người dùng cần truy cập đúng cổng đăng nhập phù hợp.<br><br><strong>1.2.E4: Không thể kết nối hoặc truy vấn cơ sở dữ liệu</strong><br>1. Hệ thống không tạo phiên đăng nhập.<br>2. Hệ thống thông báo lỗi hệ thống tạm thời và ghi nhận lỗi để quản trị viên kiểm tra.<br><br><strong>1.2.E5: Nhiều lần đăng nhập thất bại liên tiếp</strong><br>1. Hệ thống ghi nhận số lần thất bại theo tài khoản hoặc địa chỉ truy cập.<br>2. Sau 5 lần thất bại trong 15 phút, hệ thống khóa tạm tài khoản 15 phút, ghi nhật ký sự kiện và hiển thị thông báo chung; không tiết lộ trạng thái khóa chi tiết.</td>
  </tr>
  <tr>
    <td>Priority:</td>
    <td colspan="3">High</td>
  </tr>
  <tr>
    <td>Business Rules:</td>
    <td colspan="3">BR1. Người dùng có thể đăng nhập bằng tên đăng nhập hoặc email đã đăng ký, kết hợp với mật khẩu hợp lệ.<br>BR2. Thông tin xác thực phải đồng thời khớp với vai trò yêu cầu: Customer hoặc Seller/Admin.<br>BR3. Chỉ sau khi xác thực thành công, hệ thống mới tạo phiên và cấp quyền truy cập chức năng theo vai trò.<br>BR4. Phiên Customer và phiên Seller/Admin phải được nhận diện riêng để kiểm tra quyền truy cập các chức năng tương ứng.<br>BR5. Sau khi Customer đăng nhập thành công, hệ thống khôi phục giỏ hàng cá nhân nếu có; sau khi Seller/Admin đăng nhập thành công, hệ thống chuyển đến khu vực quản trị.<br>BR6. Thông báo đăng nhập thất bại không được tiết lộ tài khoản có tồn tại hay không, vai trò thực tế hoặc nguyên nhân chi tiết của việc xác thực thất bại.<br>BR7. Mật khẩu được lưu bằng BCrypt với salt ngẫu nhiên; hệ thống chỉ so sánh bằng hàm xác minh BCrypt, không lưu hoặc so sánh mật khẩu dạng văn bản thuần.<br>BR8. Sau khi đăng nhập thành công, hệ thống thay mã định danh phiên, đặt cờ HttpOnly, Secure khi vận hành HTTPS và thời hạn không hoạt động tối đa 30 phút.<br>BR9. Sau 5 lần đăng nhập thất bại liên tiếp trong 15 phút đối với cùng tài khoản, hệ thống khóa tạm tài khoản trong 15 phút và ghi nhật ký sự kiện an toàn.</td>
  </tr>
  <tr>
    <td>Other Information:</td>
    <td colspan="3">- Mô-đun xác thực gồm CustomerLoginServlet và SellerLoginServlet hoặc một endpoint thống nhất có tham số vai trò; mọi quyền vẫn được kiểm tra bằng RBAC phía máy chủ.<br>- Customer được điều hướng đến danh mục sách; Seller/Admin được điều hướng đến bảng điều khiển quản trị.<br>- HttpSession chỉ lưu định danh người dùng, vai trò và dữ liệu phiên tối thiểu; không lưu mật khẩu hoặc thông tin xác thực nhạy cảm.<br>- Cơ chế BCrypt, đổi mã phiên, khóa tạm và nhật ký đăng nhập là yêu cầu bắt buộc của mô-đun xác thực.</td>
  </tr>
</table>

**Bảng 2.5. Đặc tả Use Case UC12.2 – Hủy đơn hàng ở trạng thái cho phép hủy**

<table>
  <tr>
    <td>UC ID and<br>Name:</td>
    <td colspan="3"><strong>UC – 12.2: Hủy đơn hàng ở trạng thái cho phép hủy</strong></td>
  </tr>
  <tr>
    <td>Created By:</td>
    <td>Name</td>
    <td>Date Created:</td>
    <td>Date</td>
  </tr>
  <tr>
    <td>Primary Actor:</td>
    <td>Khách hàng (Customer)</td>
    <td>Secondary Actors:</td>
    <td>Mail Server</td>
  </tr>
  <tr>
    <td>Trigger:</td>
    <td colspan="3">Khách hàng đang đăng nhập xem lịch sử đơn hàng, chọn một đơn của chính mình ở trạng thái PENDING và gửi yêu cầu hủy đơn.</td>
  </tr>
  <tr>
    <td>Description:</td>
    <td colspan="3">Use case mô tả quá trình khách hàng hủy đơn hàng trước khi đơn được xác nhận thanh toán hoặc chuyển sang các giai đoạn xử lý tiếp theo. Hệ thống kiểm tra quyền sở hữu đơn, trạng thái đơn tại thời điểm hủy và lý do hủy; nếu hợp lệ, hệ thống chuyển đơn sang trạng thái CANCELLED, lưu thông tin hủy và gửi thư thông báo. Khách hàng chỉ được hủy đơn ở trạng thái PENDING. Đơn đã được PayOS xác nhận thanh toán ở trạng thái CONFIRMED hoặc các trạng thái sau đó không thể do khách hàng tự hủy; trường hợp này phải do quản trị viên/người bán xử lý theo UC12.4.</td>
  </tr>
  <tr>
    <td>Preconditions:</td>
    <td colspan="3">PRE-1: Khách hàng đã đăng nhập bằng tài khoản Customer.<br>PRE-2: Đơn hàng tồn tại và thuộc về chính khách hàng đang đăng nhập.<br>PRE-3: Đơn hàng đang ở trạng thái PENDING tại thời điểm hệ thống thực hiện yêu cầu hủy.<br>PRE-4: Đơn hàng chưa được xác nhận thanh toán qua PayOS và chưa chuyển sang trạng thái CONFIRMED, SHIPPING hoặc COMPLETED.</td>
  </tr>
  <tr>
    <td>Postconditions:</td>
    <td colspan="3">POST-1: Đơn hợp lệ được chuyển từ PENDING sang CANCELLED.<br>POST-2: Hệ thống lưu người hủy là CUSTOMER, lý do hủy và thời điểm hủy.<br>POST-3: Vì đơn PENDING chưa được trừ tồn kho, số lượng tồn kho không thay đổi khi khách hàng hủy đơn.<br>POST-4: Hệ thống tạo yêu cầu gửi thư thông báo hủy đơn đến khách hàng.<br>POST-5: Lịch sử đơn hàng hiển thị trạng thái đã hủy cùng lý do và thông tin người hủy.</td>
  </tr>
  <tr>
    <td>Normal Flow:</td>
    <td colspan="3"><strong>12.2.0: Khách hàng hủy đơn PENDING</strong><br>1. Khách hàng truy cập chức năng lịch sử đơn hàng.<br>2. Hệ thống hiển thị các đơn thuộc tài khoản khách hàng, kèm trạng thái hiện tại.<br>3. Khách hàng chọn hủy một đơn có trạng thái PENDING.<br>4. Hệ thống hiển thị biểu mẫu hoặc hộp thoại xác nhận hủy đơn và tiếp nhận lý do hủy, nếu có.<br>5. Khách hàng xác nhận yêu cầu hủy đơn.<br>6. Hệ thống kiểm tra phiên đăng nhập, quyền sở hữu đơn hàng và trạng thái hiện tại của đơn trực tiếp từ cơ sở dữ liệu.<br>7. Hệ thống cập nhật đơn từ PENDING sang CANCELLED; lưu cancelled_by là CUSTOMER, lý do hủy và thời điểm hủy trong cùng thao tác dữ liệu có điều kiện theo trạng thái PENDING.<br>8. Hệ thống hoàn tất cập nhật dữ liệu và tạo yêu cầu gửi thư thông báo hủy đơn.<br>9. Mail Server gửi thư thông báo hủy đơn đến khách hàng, nếu dịch vụ thư khả dụng.<br>10. Hệ thống thông báo hủy đơn thành công và hiển thị lại lịch sử đơn hàng ở trạng thái CANCELLED.</td>
  </tr>
  <tr>
    <td>Alternative Flows:</td>
    <td colspan="3"><strong>12.2.1: Khách hàng không nhập lý do hủy</strong><br>1. Ở bước 4, khách hàng không cung cấp lý do hủy.<br>2. Hệ thống sử dụng lý do mặc định “Khách hàng hủy đơn”.<br>3. Hệ thống tiếp tục từ bước 5 của Normal Flow.<br><br><strong>12.2.2: Giao dịch thanh toán được xác nhận đồng thời với yêu cầu hủy</strong><br>1. Sau bước 5, webhook PayOS xác nhận thanh toán và chuyển đơn sang CONFIRMED trước khi thao tác hủy hoàn tất.<br>2. Cập nhật có điều kiện từ PENDING sang CANCELLED không thành công.<br>3. Hệ thống tải lại trạng thái đơn, thông báo khách hàng không thể tự hủy đơn đã được xác nhận thanh toán và hướng dẫn liên hệ cửa hàng.<br>4. Đơn chỉ có thể được xử lý bởi người bán/quản trị viên theo UC12.4.</td>
  </tr>
  <tr>
    <td>Exceptions:</td>
    <td colspan="3"><strong>12.2.E1: Khách hàng chưa đăng nhập hoặc phiên không hợp lệ</strong><br>1. Hệ thống không hủy đơn hàng.<br>2. Hệ thống yêu cầu khách hàng đăng nhập.<br><br><strong>12.2.E2: Đơn hàng không tồn tại hoặc không thuộc khách hàng</strong><br>1. Hệ thống không thay đổi đơn hàng.<br>2. Hệ thống thông báo khách hàng không có quyền thao tác trên đơn này.<br><br><strong>12.2.E3: Đơn không ở trạng thái PENDING</strong><br>1. Hệ thống từ chối yêu cầu hủy của khách hàng.<br>2. Hệ thống hiển thị trạng thái hiện tại của đơn và hướng dẫn liên hệ cửa hàng nếu cần hỗ trợ.<br><br><strong>12.2.E4: Lỗi cơ sở dữ liệu khi cập nhật hủy đơn</strong><br>1. Hệ thống hoàn tác cập nhật đang thực hiện.<br>2. Hệ thống giữ nguyên trạng thái đơn, không gửi thư hủy và thông báo khách hàng thử lại sau.<br><br><strong>12.2.E5: Không gửi được thư thông báo hủy</strong><br>1. Hệ thống giữ nguyên kết quả hủy đơn đã được cập nhật thành công.<br>2. Hệ thống ghi nhận lỗi gửi thư và hỗ trợ gửi lại thông báo sau khi dịch vụ thư hoạt động.</td>
  </tr>
  <tr>
    <td>Priority:</td>
    <td colspan="3">High</td>
  </tr>
  <tr>
    <td>Business Rules:</td>
    <td colspan="3">BR1. Khách hàng chỉ được hủy đơn hàng thuộc chính tài khoản của mình.<br>BR2. Khách hàng chỉ được tự hủy đơn ở trạng thái PENDING.<br>BR3. Hệ thống phải kiểm tra lại trạng thái đơn khi thực hiện cập nhật, sử dụng điều kiện trạng thái PENDING để tránh hủy đơn đã thay đổi trạng thái đồng thời.<br>BR4. Khi hủy thành công, hệ thống phải lưu trạng thái CANCELLED, người hủy là CUSTOMER, lý do hủy và thời điểm hủy.<br>BR5. Đơn PENDING chưa được trừ tồn kho, vì vậy khách hàng hủy đơn PENDING không làm tăng tồn kho.<br>BR6. Đơn CONFIRMED là đơn đã được thanh toán/xác nhận; khách hàng không được tự hủy. Nếu cần hủy, người bán/quản trị viên thực hiện theo UC12.4 và phải xử lý hoàn tồn kho cùng chính sách hoàn tiền phù hợp.<br>BR7. Một đơn đã ở trạng thái CANCELLED, SHIPPING hoặc COMPLETED không được hủy lại.<br>BR8. Gửi thư hủy đơn là hoạt động sau xử lý; lỗi gửi thư không được làm đảo ngược trạng thái CANCELLED đã lưu thành công.<br>BR9. Chức năng hủy đơn PENDING không thực hiện hoàn tồn kho; hệ thống chỉ hoàn tồn trong giao dịch hủy đơn CONFIRMED do Seller/Admin thực hiện theo UC12.4.</td>
  </tr>
  <tr>
    <td>Other Information:</td>
    <td colspan="3">- Endpoint thiết kế: POST /orders-action với action customer_cancel.<br>- Trường dữ liệu hủy đơn gồm status, cancelled_by, cancel_reason và cancelled_at.<br>- Thư hủy được đưa vào hàng đợi xử lý nền; kết quả hủy đơn không phụ thuộc vào kết quả gửi thư.<br>- Quy tắc tồn kho thống nhất: chỉ UC06.4 trừ tồn sau thanh toán thành công; hủy PENDING không làm thay đổi tồn kho.</td>
  </tr>
</table>

**Bảng 2.6. Đặc tả Use Case UC12.4 – Cập nhật trạng thái xử lý đơn**

<table>
  <tr><td>UC ID and<br>Name:</td><td colspan="3"><strong>UC – 12.4: Cập nhật trạng thái xử lý đơn</strong></td></tr>
  <tr><td>Created By:</td><td>Name</td><td>Date Created:</td><td>Date</td></tr>
  <tr><td>Primary Actor:</td><td>Quản trị viên/Người bán (Seller/Admin)</td><td>Secondary Actors:</td><td>Khách hàng (Customer), Mail Server</td></tr>
  <tr><td>Trigger:</td><td colspan="3">Quản trị viên/người bán mở danh sách đơn hàng và thực hiện thao tác chuyển trạng thái cho một đơn hàng cần xử lý.</td></tr>
  <tr><td>Description:</td><td colspan="3">Use case mô tả quá trình quản trị viên/người bán cập nhật vòng đời xử lý đơn hàng sau khi thanh toán đã được xác nhận. Hệ thống kiểm tra quyền của người thao tác, trạng thái hiện tại của đơn và quy tắc chuyển trạng thái trước khi lưu thay đổi. Vòng đời chính của đơn là PENDING → CONFIRMED → SHIPPING → COMPLETED; đơn có thể chuyển sang CANCELLED theo điều kiện cho phép. Việc xác nhận thanh toán qua PayOS thuộc UC06.4, không được thay thế bằng thao tác xác nhận thủ công của quản trị viên.</td></tr>
  <tr><td>Preconditions:</td><td colspan="3">PRE-1: Người thao tác đã đăng nhập với vai trò Seller/Admin.<br>PRE-2: Đơn hàng tồn tại trong hệ thống.<br>PRE-3: Trạng thái hiện tại của đơn cho phép chuyển sang trạng thái mới theo quy tắc nghiệp vụ.<br>PRE-4: Với chuyển trạng thái CONFIRMED → SHIPPING, đơn hàng đã được xác nhận thanh toán hợp lệ qua UC06.4.</td></tr>
  <tr><td>Postconditions:</td><td colspan="3">POST-1: Khi yêu cầu hợp lệ, trạng thái đơn hàng được cập nhật theo đúng quy tắc chuyển trạng thái.<br>POST-2: Khi đơn chuyển sang SHIPPING, hệ thống lưu thời điểm bắt đầu giao hàng.<br>POST-3: Khi đơn bị hủy bởi người bán, hệ thống lưu người hủy, lý do hủy và thời điểm hủy; tồn kho chỉ được hoàn lại nếu trước đó đã bị trừ khi xác nhận thanh toán.<br>POST-4: Khách hàng có thể xem trạng thái mới của đơn trong lịch sử đơn hàng.<br>POST-5: Hệ thống gửi hoặc xếp yêu cầu gửi thông báo trạng thái cho khách hàng theo cơ chế thông báo được cấu hình.</td></tr>
  <tr><td>Normal Flow:</td><td colspan="3"><strong>12.4.0: Cập nhật trạng thái xử lý đơn</strong><br>1. Quản trị viên/người bán truy cập danh sách đơn hàng.<br>2. Hệ thống hiển thị danh sách đơn, trạng thái hiện tại và các thao tác tương ứng với từng trạng thái.<br>3. Quản trị viên/người bán chọn một đơn hàng và trạng thái đích hợp lệ.<br>4. Hệ thống kiểm tra phiên đăng nhập Seller/Admin và quyền thao tác.<br>5. Hệ thống tải lại trạng thái hiện tại của đơn hàng từ cơ sở dữ liệu.<br>6. Hệ thống kiểm tra tính hợp lệ của chuyển trạng thái theo quy tắc nghiệp vụ.<br>7. Hệ thống cập nhật trạng thái đơn bằng thao tác có điều kiện theo trạng thái hiện tại để tránh thay đổi đồng thời không nhất quán.<br>8. Nếu trạng thái mới là SHIPPING, hệ thống lưu thời điểm giao hàng.<br>9. Hệ thống hoàn tất cập nhật, tạo thông báo cho khách hàng và hiển thị kết quả cho quản trị viên/người bán.<br>10. Khách hàng xem được trạng thái mới trong lịch sử đơn hàng.</td></tr>
  <tr><td>Alternative Flows:</td><td colspan="3"><strong>12.4.1: Chuyển đơn từ CONFIRMED sang SHIPPING</strong><br>1. Quản trị viên/người bán chọn thao tác chuyển giao hàng đối với đơn CONFIRMED.<br>2. Hệ thống xác minh đơn đã được xác nhận thanh toán và đang ở trạng thái CONFIRMED.<br>3. Hệ thống cập nhật trạng thái thành SHIPPING và ghi thời điểm hiện tại là thời điểm giao hàng.<br>4. Hệ thống thông báo trạng thái đang giao hàng cho khách hàng.<br><br><strong>12.4.2: Hoàn tất đơn đang giao</strong><br>1. Quản trị viên/người bán chọn thao tác hoàn thành đối với đơn SHIPPING.<br>2. Hệ thống cập nhật trạng thái đơn thành COMPLETED.<br>3. Hệ thống thông báo giao dịch hoàn tất cho khách hàng.<br><br><strong>12.4.3: Tự động hoàn tất đơn SHIPPING</strong><br>1. Bộ lập lịch phát hiện đơn SHIPPING đã quá 3 ngày kể từ thời điểm giao mà chưa được xử lý khác.<br>2. Hệ thống chuyển đơn sang COMPLETED theo quy tắc tự động của hệ thống.<br>3. Hệ thống ghi nhận kết quả và thông báo cho khách hàng nếu cơ chế thông báo được bật.<br><br><strong>12.4.4: Người bán hủy đơn</strong><br>1. Quản trị viên/người bán chọn hủy đơn PENDING hoặc CONFIRMED và nhập lý do hủy.<br>2. Hệ thống kiểm tra trạng thái hiện tại và xác định đơn có được hủy hay không.<br>3. Hệ thống cập nhật đơn sang CANCELLED, lưu người hủy là SELLER, lý do và thời điểm hủy.<br>4. Nếu đơn đã CONFIRMED, hệ thống hoàn lại số lượng tồn kho của các sách trong đơn trong cùng giao dịch dữ liệu.<br>5. Hệ thống gửi thông báo hủy đơn đến khách hàng.<br><br><strong>12.4.5: Khách hàng xác nhận đã nhận hàng</strong><br>1. Khách hàng sử dụng UC12.1 để xác nhận nhận hàng khi đơn đang SHIPPING.<br>2. Hệ thống kiểm tra quyền sở hữu đơn và chuyển trạng thái sang COMPLETED.<br>3. Hệ thống hiển thị kết quả hoàn tất đơn hàng.</td></tr>
  <tr><td>Exceptions:</td><td colspan="3"><strong>12.4.E1: Người thao tác không có quyền Seller/Admin</strong><br>1. Hệ thống từ chối yêu cầu cập nhật trạng thái.<br>2. Hệ thống thông báo người dùng không có quyền thực hiện thao tác này.<br><br><strong>12.4.E2: Đơn hàng không tồn tại</strong><br>1. Hệ thống không thực hiện cập nhật.<br>2. Hệ thống thông báo đơn hàng không tồn tại hoặc đã bị xóa khỏi ngữ cảnh xử lý.<br><br><strong>12.4.E3: Chuyển trạng thái không hợp lệ</strong><br>1. Hệ thống không cập nhật đơn hàng.<br>2. Hệ thống thông báo trạng thái hiện tại không cho phép chuyển sang trạng thái được yêu cầu.<br><br><strong>12.4.E4: Trạng thái đơn thay đổi đồng thời</strong><br>1. Hệ thống cập nhật có điều kiện không thành công vì đơn đã được webhook, khách hàng hoặc tác vụ khác thay đổi trạng thái.<br>2. Hệ thống tải lại trạng thái mới nhất và yêu cầu người thao tác kiểm tra lại trước khi thực hiện thao tác khác.<br><br><strong>12.4.E5: Lỗi cơ sở dữ liệu khi cập nhật hoặc hoàn tồn kho</strong><br>1. Hệ thống hoàn tác toàn bộ giao dịch dữ liệu.<br>2. Hệ thống giữ nguyên trạng thái và tồn kho trước thao tác.<br>3. Hệ thống thông báo lỗi để người bán thử lại hoặc liên hệ quản trị kỹ thuật.<br><br><strong>12.4.E6: Không gửi được thông báo trạng thái</strong><br>1. Hệ thống giữ nguyên kết quả cập nhật trạng thái đã thành công.<br>2. Hệ thống ghi nhận lỗi thông báo và đưa vào cơ chế gửi lại khi dịch vụ sẵn sàng.</td></tr>
  <tr><td>Priority:</td><td colspan="3">High</td></tr>
  <tr><td>Business Rules:</td><td colspan="3">BR1. Chỉ Seller/Admin được cập nhật trạng thái xử lý đơn; Customer chỉ được xác nhận nhận hàng khi đơn đang SHIPPING hoặc hủy đơn theo UC12.2.<br>BR2. Chuyển trạng thái xử lý hợp lệ là CONFIRMED → SHIPPING → COMPLETED.<br>BR3. PENDING → CONFIRMED là kết quả xác nhận thanh toán của UC06.4. Seller/Admin không được thủ công xác nhận đơn PENDING.<br>BR4. Seller/Admin chỉ được hủy đơn ở PENDING hoặc CONFIRMED; đơn SHIPPING và COMPLETED không được hủy trực tiếp bằng chức năng này.<br>BR5. Khi Seller/Admin hủy đơn CONFIRMED, hệ thống phải hoàn lại tồn kho của từng chi tiết đơn trong cùng giao dịch với cập nhật CANCELLED. Khi hủy PENDING, hệ thống không hoàn tồn vì chưa trừ kho.<br>BR6. Khi chuyển sang SHIPPING, hệ thống phải lưu thời điểm giao hàng; đơn SHIPPING quá 3 ngày tự động chuyển sang COMPLETED bằng tác vụ nền kiểm tra mỗi 30 phút.<br>BR7. Mọi cập nhật trạng thái phải kiểm tra trạng thái hiện tại trong điều kiện cập nhật để bảo đảm một đơn không bị chuyển trạng thái sai do xử lý đồng thời.<br>BR8. Hệ thống gửi email và thông báo trong hệ thống cho khách hàng khi đơn chuyển sang CONFIRMED, SHIPPING, COMPLETED hoặc CANCELLED. Lỗi thông báo được xử lý độc lập qua hàng đợi gửi lại.<br>BR9. Hành động Seller/Admin chỉ gồm ship, complete và cancel; hành động confirm không được cung cấp trong giao diện hay endpoint quản trị.<br>BR10. Khi hủy PENDING, hệ thống không cộng lại tồn kho; chỉ hoàn tồn đối với đơn CONFIRMED đã bị trừ kho sau xác nhận thanh toán.</td></tr>
  <tr><td>Other Information:</td><td colspan="3">- Endpoint thiết kế: POST /orders-action; các action quản trị gồm ship, complete và cancel; xác nhận thanh toán được xử lý độc quyền bởi POST /api/payos-webhook.<br>- Trạng thái sử dụng: PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED và EXPIRED.<br>- Tác vụ nền tự động hoàn tất đơn SHIPPING quá 3 ngày, chạy kiểm tra định kỳ mỗi 30 phút.<br>- Thông báo trạng thái sử dụng email và thông báo trong hệ thống, được gửi qua hàng đợi nền.</td></tr>
</table>

**Bảng 2.7. Đặc tả Use Case UC10.2 – Cập nhật danh mục sách**

<table>
  <tr><td>UC ID and<br>Name:</td><td colspan="3"><strong>UC – 10.2: Cập nhật danh mục sách</strong></td></tr>
  <tr><td>Created By:</td><td>Name</td><td>Date Created:</td><td>Date</td></tr>
  <tr><td>Primary Actor:</td><td>Quản trị viên/Người bán (Seller/Admin)</td><td>Secondary Actors:</td><td>None</td></tr>
  <tr><td>Trigger:</td><td colspan="3">Quản trị viên/người bán cần bổ sung, chỉnh sửa hoặc ngừng hiển thị một đầu sách trong danh mục bán hàng.</td></tr>
  <tr><td>Description:</td><td colspan="3">Use case mô tả việc quản trị viên/người bán duy trì danh mục sách. Với mỗi đầu sách, hệ thống hiện quản lý các trường <em>barcode</em>, <em>name</em>, <em>author</em>, <em>price</em> và <em>quantity</em> trong bảng <em>books</em>. Người thao tác có thể tạo mới, cập nhật thông tin hoặc gỡ đầu sách khỏi danh mục khi dữ liệu hợp lệ. Thay đổi được lưu vào kho dữ liệu để danh sách sách, giỏ hàng, đơn hàng và các thống kê sử dụng thông tin nhất quán.</td></tr>
  <tr><td>Preconditions:</td><td colspan="3">PRE-1: Người thao tác đã đăng nhập với vai trò Seller/Admin.<br>PRE-2: Với thao tác cập nhật hoặc gỡ sách, đầu sách có mã <em>barcode</em> cần xử lý tồn tại trong bảng <em>books</em>.<br>PRE-3: Người thao tác có đủ thông tin hợp lệ của đầu sách; với thao tác tạo mới, mã <em>barcode</em> chưa tồn tại.<br>PRE-4: Cơ sở dữ liệu sẵn sàng ghi nhận thay đổi.</td></tr>
  <tr><td>Postconditions:</td><td colspan="3">POST-1: Khi thông tin hợp lệ, đầu sách được thêm mới hoặc cập nhật trong bảng <em>books</em>.<br>POST-2: Khi gỡ sách thành công, đầu sách không còn xuất hiện trong danh mục bán hàng.<br>POST-3: Khi có lỗi kiểm tra dữ liệu hoặc lỗi lưu trữ, dữ liệu sách giữ nguyên trạng thái trước thao tác.<br>POST-4: Danh sách kho sách hiển thị dữ liệu mới nhất sau khi thao tác thành công.</td></tr>
  <tr><td>Normal Flow:</td><td colspan="3"><strong>10.2.0: Cập nhật thông tin đầu sách</strong><br>1. Quản trị viên/người bán truy cập chức năng quản lý kho sách/danh mục sách.<br>2. Hệ thống kiểm tra phiên đăng nhập và quyền Seller/Admin, sau đó hiển thị danh sách đầu sách.<br>3. Người thao tác chọn đầu sách cần chỉnh sửa.<br>4. Hệ thống tải thông tin sách theo <em>barcode</em> và hiển thị biểu mẫu cập nhật.<br>5. Người thao tác chỉnh sửa các trường cho phép: <em>name</em>, <em>author</em>, <em>price</em>, <em>quantity</em>, <em>image_url</em>, <em>description</em>, <em>category_id</em>, <em>publisher</em> và trạng thái hiển thị; <em>barcode</em> là mã định danh và không được thay đổi.<br>6. Người thao tác gửi yêu cầu lưu thay đổi.<br>7. Hệ thống kiểm tra tính đầy đủ, kiểu dữ liệu, miền giá trị và quyền thao tác.<br>8. Hệ thống cập nhật đúng đầu sách có <em>barcode</em> tương ứng trong bảng <em>books</em>.<br>9. Hệ thống thông báo kết quả và hiển thị lại danh sách/kho sách.</td></tr>
  <tr><td>Alternative Flows:</td><td colspan="3"><strong>10.2.1: Thêm đầu sách mới</strong><br>1. Ở bước 3, người thao tác chọn thêm sách mới.<br>2. Hệ thống hiển thị biểu mẫu nhập <em>barcode</em>, <em>name</em>, <em>author</em>, <em>price</em>, <em>quantity</em>, <em>category_id</em>, <em>publisher</em>, <em>image_url</em>, <em>description</em> và trạng thái hiển thị.<br>3. Người thao tác nhập thông tin và gửi yêu cầu thêm mới.<br>4. Hệ thống kiểm tra mã <em>barcode</em> chưa tồn tại, danh mục hợp lệ và các giá trị hợp lệ.<br>5. Hệ thống tạo bản ghi sách mới trong bảng <em>books</em>, sau đó thông báo thành công.<br><br><strong>10.2.2: Ngừng kinh doanh đầu sách</strong><br>1. Ở bước 3, người thao tác chọn ngừng kinh doanh một đầu sách.<br>2. Hệ thống yêu cầu xác nhận thao tác và kiểm tra đầu sách tồn tại.<br>3. Hệ thống đặt <em>status</em> của đầu sách thành INACTIVE thay vì xóa vật lý.<br>4. Sách bị ẩn khỏi danh mục công khai và không thể thêm vào giỏ, nhưng dữ liệu giao dịch lịch sử vẫn được giữ lại.<br><br><strong>10.2.3: Nhập nhiều đầu sách từ tệp</strong><br>1. Người thao tác tải tệp CSV mã hóa UTF-8 gồm các cột: barcode, name, author, price, quantity, category_id, publisher, image_url, description và status.<br>2. Hệ thống kiểm tra từng dòng, mã <em>barcode</em> trùng lặp và tồn tại của <em>category_id</em>.<br>3. Nếu có bất kỳ dòng lỗi, hệ thống từ chối toàn bộ tệp và trả về danh sách số dòng cùng lỗi tương ứng.<br>4. Nếu toàn bộ dữ liệu hợp lệ, hệ thống thêm các bản ghi trong một giao dịch dữ liệu và thông báo số đầu sách đã nhập.</td></tr>
  <tr><td>Exceptions:</td><td colspan="3"><strong>10.2.E1: Người thao tác chưa đăng nhập hoặc không có quyền Seller/Admin</strong><br>1. Hệ thống không cho phép truy cập hoặc lưu thay đổi.<br>2. Hệ thống chuyển đến trang đăng nhập hoặc thông báo không đủ quyền.<br><br><strong>10.2.E2: Thiếu hoặc sai định dạng dữ liệu</strong><br>1. Hệ thống không lưu dữ liệu.<br>2. Hệ thống chỉ rõ trường cần hiệu chỉnh; dữ liệu người dùng đã nhập được giữ lại để nhập lại.<br><br><strong>10.2.E3: Mã barcode trùng khi thêm mới</strong><br>1. Hệ thống từ chối tạo bản ghi mới.<br>2. Hệ thống thông báo mã sách đã tồn tại và cho phép người thao tác chuyển sang cập nhật đầu sách đó.<br><br><strong>10.2.E4: Đầu sách không tồn tại khi cập nhật hoặc gỡ</strong><br>1. Hệ thống không thực hiện thay đổi.<br>2. Hệ thống thông báo dữ liệu đã bị thay đổi hoặc không còn tồn tại và tải lại danh sách sách.<br><br><strong>10.2.E5: Không thể gỡ sách do ràng buộc dữ liệu</strong><br>1. Hệ thống không gỡ bản ghi sách.<br>2. Hệ thống thông báo đầu sách đang liên kết với dữ liệu nghiệp vụ, chẳng hạn chi tiết đơn hàng, và đề xuất ngừng kinh doanh/ẩn sách khi có chức năng tương ứng.<br><br><strong>10.2.E6: Lỗi cơ sở dữ liệu</strong><br>1. Hệ thống hoàn tác thao tác đang thực hiện.<br>2. Hệ thống thông báo lưu thất bại và không hiển thị trạng thái thành công.</td></tr>
  <tr><td>Priority:</td><td colspan="3">High</td></tr>
  <tr><td>Business Rules:</td><td colspan="3">BR1. Chỉ Seller/Admin được thêm, cập nhật hoặc ngừng kinh doanh đầu sách; khách hàng chỉ được xem danh mục công khai.<br>BR2. <em>barcode</em> là mã định danh của sách, phải duy nhất và không được thay đổi khi cập nhật thông tin đầu sách được thiết kế.<br>BR3. <em>name</em>, <em>author</em> và <em>category_id</em> là thông tin bắt buộc, không được rỗng sau khi loại bỏ khoảng trắng thừa.<br>BR4. <em>price</em> phải là số lớn hơn 0; <em>quantity</em> phải là số nguyên không âm; <em>status</em> chỉ nhận ACTIVE hoặc INACTIVE.<br>BR5. Mọi giá trị do người dùng nhập phải được kiểm tra ở phía máy chủ trước khi ghi dữ liệu; dữ liệu khi hiển thị phải được mã hóa HTML để ngăn chèn mã thực thi.<br>BR6. Hệ thống không xóa vật lý đầu sách đã phát sinh giao dịch. Muốn ngừng bán, Seller/Admin chuyển <em>status</em> sang INACTIVE; dữ liệu lịch sử được giữ nguyên.<br>BR7. Hệ thống có bảng <em>categories</em> với quan hệ khóa ngoại <em>books.category_id</em> tham chiếu <em>categories.id</em>. Các nhóm Bán chạy, Nổi bật và Mới về là danh mục dẫn xuất theo quy tắc truy vấn, không thay thế thể loại sách.<br>BR8. Nhập CSV là thao tác nguyên tử: hoặc toàn bộ dòng hợp lệ được lưu, hoặc không có dòng nào được lưu.</td></tr>
  <tr><td>Other Information:</td><td colspan="3">- Endpoint thiết kế: GET/POST /admin/books, POST /admin/books/import và POST /admin/books/{barcode}/deactivate.<br>- Dịch vụ thiết kế: BookService; các nghiệp vụ gồm tạo, cập nhật, đổi trạng thái hiển thị và nhập tệp CSV.<br>- Bảng <em>books</em> được mở rộng gồm barcode, name, author, price, quantity, category_id, publisher, image_url, description, status, created_at và updated_at; bảng <em>categories</em> lưu thể loại sách.<br>- Mọi thao tác thay đổi danh mục được ghi nhật ký quản trị gồm người thực hiện, thời điểm, loại thao tác và dữ liệu trước/sau thay đổi.</td></tr>
</table>

**Bảng 2.8. Đặc tả Use Case UC08.2 – Kiểm duyệt yêu cầu ký gửi**

<table>
  <tr><td>UC ID and<br>Name:</td><td colspan="3"><strong>UC – 08.2: Kiểm duyệt yêu cầu ký gửi</strong></td></tr>
  <tr><td>Created By:</td><td>Name</td><td>Date Created:</td><td>Date</td></tr>
  <tr><td>Primary Actor:</td><td>Quản trị viên/Người bán (Seller/Admin)</td><td>Secondary Actors:</td><td>Khách hàng ký gửi (Customer), Hệ thống thông báo quản trị</td></tr>
  <tr><td>Trigger:</td><td colspan="3">Quản trị viên/người bán nhận được thông báo hoặc mở danh sách các yêu cầu ký gửi sách cũ đang có trạng thái <em>PENDING</em>.</td></tr>
  <tr><td>Description:</td><td colspan="3">Use case mô tả việc kiểm tra và ra quyết định đối với yêu cầu ký gửi sách cũ của khách hàng. Người duyệt xem thông tin được lưu trong bảng <em>used_books</em>, gồm người ký gửi, tên sách, tác giả, giá gốc, giá bán lại, độ mới, ảnh thực tế và mô tả. Nếu yêu cầu đáp ứng tiêu chí, hệ thống chuyển trạng thái từ <em>PENDING</em> sang <em>APPROVED</em> để sách xuất hiện trong danh mục “Góc Sách Tái Sinh”; nếu không đáp ứng, hệ thống chuyển sang <em>REJECTED</em> và phản hồi lý do khi cơ chế phản hồi được hoàn thiện.</td></tr>
  <tr><td>Preconditions:</td><td colspan="3">PRE-1: Người thao tác đã đăng nhập với vai trò Seller/Admin.<br>PRE-2: Yêu cầu ký gửi tồn tại trong bảng <em>used_books</em> và đang ở trạng thái <em>PENDING</em>.<br>PRE-3: Yêu cầu đã có các thông tin bắt buộc: <em>seller_username</em>, <em>book_title</em>, <em>author</em>, <em>original_price</em>, <em>resale_price</em> và <em>condition_percent</em>.<br>PRE-4: Người duyệt có thể truy cập ảnh <em>image_url</em> và mô tả <em>description</em> nếu khách hàng đã cung cấp.</td></tr>
  <tr><td>Postconditions:</td><td colspan="3">POST-1: Khi duyệt, trạng thái yêu cầu được cập nhật từ <em>PENDING</em> sang <em>APPROVED</em>; sách được phép hiển thị trong danh mục sách cũ.<br>POST-2: Khi từ chối, trạng thái yêu cầu được cập nhật từ <em>PENDING</em> sang <em>REJECTED</em>; yêu cầu không xuất hiện trong danh mục sách cũ công khai.<br>POST-3: Khách hàng ký gửi có thể xem kết quả xử lý yêu cầu của mình.<br>POST-4: Thông báo quản trị liên quan được đánh dấu đã xử lý/đã đọc nếu cơ chế thông báo hỗ trợ thao tác này.<br>POST-5: Khi cập nhật không thành công, trạng thái yêu cầu vẫn là <em>PENDING</em>.</td></tr>
  <tr><td>Normal Flow:</td><td colspan="3"><strong>08.2.0: Duyệt yêu cầu ký gửi</strong><br>1. Quản trị viên/người bán mở chuông thông báo hoặc trang quản lý yêu cầu ký gửi.<br>2. Hệ thống kiểm tra quyền Seller/Admin và hiển thị danh sách yêu cầu ở trạng thái <em>PENDING</em>.<br>3. Người duyệt chọn một yêu cầu.<br>4. Hệ thống hiển thị chi tiết: <em>seller_username</em>, <em>book_title</em>, <em>author</em>, <em>original_price</em>, <em>resale_price</em>, <em>condition_percent</em>, <em>image_url</em> và <em>description</em>.<br>5. Người duyệt kiểm tra tính đầy đủ, mức độ phù hợp của thông tin và tình trạng thực tế thể hiện trên ảnh.<br>6. Người duyệt chọn duyệt yêu cầu.<br>7. Hệ thống tải lại trạng thái yêu cầu và xác minh yêu cầu vẫn là <em>PENDING</em>.<br>8. Hệ thống cập nhật trạng thái thành <em>APPROVED</em>, ghi nhận kết quả xử lý và cập nhật thông báo liên quan trong cùng đơn vị xử lý dữ liệu khi phù hợp.<br>9. Hệ thống hiển thị kết quả; đầu sách được đưa vào danh mục “Góc Sách Tái Sinh” để khách hàng khác có thể xem và mua.</td></tr>
  <tr><td>Alternative Flows:</td><td colspan="3"><strong>08.2.1: Từ chối yêu cầu ký gửi</strong><br>1. Tại bước 6, người duyệt chọn từ chối do thông tin, ảnh hoặc tình trạng sách không đáp ứng tiêu chí.<br>2. Hệ thống yêu cầu người duyệt nhập lý do từ chối, tối thiểu 10 ký tự.<br>3. Hệ thống kiểm tra yêu cầu vẫn ở trạng thái <em>PENDING</em>, sau đó chuyển trạng thái thành <em>REJECTED</em>.<br>4. Hệ thống lưu <em>rejection_reason</em>, <em>reviewed_by</em>, <em>reviewed_at</em> và gửi thông báo kết quả cho khách hàng ký gửi.<br><br><strong>08.2.2: Yêu cầu bổ sung thông tin</strong><br>1. Tại bước 5, người duyệt xác định yêu cầu thiếu ảnh hoặc thông tin cần làm rõ nhưng chưa cần từ chối.<br>2. Người duyệt nhập nội dung cần bổ sung; hệ thống chuyển trạng thái sang <em>NEED_MORE_INFO</em>, lưu người duyệt và thời điểm xử lý, đồng thời gửi thông báo đến khách hàng.<br>3. Khách hàng có 7 ngày để bổ sung thông tin và gửi lại yêu cầu; khi gửi lại, trạng thái chuyển về <em>PENDING</em> để duyệt lại.<br>4. Sau 7 ngày không phản hồi, hệ thống tự động chuyển yêu cầu sang <em>REJECTED</em> với lý do “Quá hạn bổ sung thông tin”.</td></tr>
  <tr><td>Exceptions:</td><td colspan="3"><strong>08.2.E1: Người thao tác không có quyền Seller/Admin</strong><br>1. Hệ thống không hiển thị hoặc không cho phép xử lý yêu cầu.<br>2. Hệ thống thông báo không đủ quyền truy cập.<br><br><strong>08.2.E2: Yêu cầu không tồn tại hoặc không còn ở trạng thái PENDING</strong><br>1. Hệ thống không cập nhật trạng thái.<br>2. Hệ thống tải lại danh sách và thông báo yêu cầu đã được xử lý hoặc không tồn tại.<br><br><strong>08.2.E3: Dữ liệu yêu cầu không đầy đủ hoặc ảnh không truy cập được</strong><br>1. Hệ thống không cho phép duyệt ngay.<br>2. Hệ thống hiển thị trường thiếu hoặc lỗi tệp và cho phép từ chối/yêu cầu bổ sung theo quy tắc được cấu hình.<br><br><strong>08.2.E4: Hai người duyệt xử lý đồng thời</strong><br>1. Hệ thống cập nhật có điều kiện từ <em>PENDING</em> không thành công với yêu cầu đến sau.<br>2. Hệ thống tải lại trạng thái mới nhất, không ghi đè kết quả đã có và thông báo cho người thao tác.<br><br><strong>08.2.E5: Lỗi cơ sở dữ liệu hoặc hệ thống thông báo</strong><br>1. Nếu cập nhật trạng thái thất bại, hệ thống hoàn tác thao tác và giữ trạng thái <em>PENDING</em>.<br>2. Nếu cập nhật trạng thái thành công nhưng gửi thông báo thất bại, hệ thống giữ kết quả duyệt/từ chối, ghi nhận lỗi và đưa thông báo vào cơ chế gửi lại.</td></tr>
  <tr><td>Priority:</td><td colspan="3">High</td></tr>
  <tr><td>Business Rules:</td><td colspan="3">BR1. Chỉ Seller/Admin được phê duyệt, từ chối hoặc yêu cầu bổ sung thông tin; khách hàng chỉ được tạo, sửa khi được yêu cầu bổ sung và theo dõi yêu cầu thuộc tài khoản của mình.<br>BR2. Chỉ yêu cầu có trạng thái <em>PENDING</em> được chuyển sang <em>APPROVED</em>, <em>REJECTED</em> hoặc <em>NEED_MORE_INFO</em>.<br>BR3. Một yêu cầu <em>APPROVED</em> mới được hiển thị công khai trong “Góc Sách Tái Sinh”; yêu cầu <em>PENDING</em>, <em>NEED_MORE_INFO</em> và <em>REJECTED</em> không được hiển thị công khai.<br>BR4. Một sách cũ được thanh toán thành công sẽ chuyển sang <em>SOLD</em> theo luồng mua sách cũ; UC08.2 không được chuyển trực tiếp sang <em>SOLD</em>.<br>BR5. Điều kiện duyệt: <em>condition_percent</em> là số nguyên từ 70 đến 100; <em>original_price</em> và <em>resale_price</em> lớn hơn 0; <em>resale_price</em> không vượt <em>original_price</em>; có ít nhất một ảnh thực tế; tên sách và tác giả không rỗng; ảnh/nội dung không vi phạm pháp luật, quyền sở hữu trí tuệ hoặc chuẩn mực cộng đồng.<br>BR6. Khi từ chối hoặc yêu cầu bổ sung, hệ thống bắt buộc lưu <em>rejection_reason</em> hoặc <em>request_more_info_reason</em>, <em>reviewed_by</em> và <em>reviewed_at</em>; khách hàng được xem nội dung phản hồi.<br>BR7. Hệ thống phải kiểm tra trạng thái hiện tại trong điều kiện cập nhật để một yêu cầu không thể được duyệt và từ chối đồng thời.<br>BR8. Khi sách đã chuyển sang <em>SOLD</em>, hệ thống cộng Book Coins cho tài khoản <em>seller_username</em> bằng 10% giá trị <em>resale_price</em>, làm tròn xuống số nguyên; thao tác cập nhật trạng thái SOLD và cộng xu phải nằm trong cùng giao dịch dữ liệu.</td></tr>
  <tr><td>Other Information:</td><td colspan="3">- Bảng <em>used_books</em> gồm id, seller_username, book_title, author, original_price, resale_price, condition_percent, image_url, description, status, rejection_reason, request_more_info_reason, reviewed_by, reviewed_at và created_at.<br>- Trạng thái sử dụng: PENDING, NEED_MORE_INFO, APPROVED, REJECTED và SOLD.<br>- Endpoint thiết kế: GET /admin/used-books, POST /admin/used-books/{id}/approve, POST /admin/used-books/{id}/reject và POST /admin/used-books/{id}/request-more-info.<br>- Khi tạo yêu cầu ký gửi, hệ thống thêm thông báo NEW_USED_BOOK vào <em>admin_notifications</em>; khi duyệt có kết quả, hệ thống tạo thông báo trong hệ thống cho khách hàng ký gửi.</td></tr>
</table>
