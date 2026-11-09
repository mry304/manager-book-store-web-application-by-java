# Hiệu sách trực tuyến

Ứng dụng bán sách trực tuyến xây dựng bằng Java Servlet, JDBC, Maven và MySQL. Dự án được đóng gói dưới dạng WAR để triển khai trên Apache Tomcat.

## Yêu cầu

- JDK 17 (không phải chỉ JRE); kiểm tra bằng `java -version` và `javac -version`.
- Apache Maven 3.8 trở lên.
- MySQL 8.x.
- Apache Tomcat 10.1 trở lên. Dự án dùng Jakarta Servlet 6 (`jakarta.servlet`), nên không tương thích với Tomcat 9 trở xuống.

Git và MySQL Workbench là tùy chọn nhưng hữu ích khi thiết lập dự án.

## Cài đặt và chạy cục bộ

### 1. Lấy mã nguồn

```powershell
git clone <repository-url>
Set-Location selling-book-store-main
```

### 2. Tạo cơ sở dữ liệu

Đăng nhập MySQL bằng tài khoản có quyền tạo database, rồi chạy hai script theo thứ tự sau:

```powershell
mysql -u root -p < setup/CreateDatastore.sql
mysql -u root -p < setup/AlterOrdersTable.sql
```

`CreateDatastore.sql` tạo database `onlinebookstore` và các bảng chính. `AlterOrdersTable.sql` là script cập nhật an toàn để bổ sung các trường vòng đời đơn hàng cho cả database mới lẫn database đã tồn tại.

### 3. Cấu hình kết nối MySQL

Mở `src/main/resources/application.properties` và điều chỉnh các giá trị cho máy của bạn:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.host=jdbc:mysql://localhost
db.port=3306
db.name=onlinebookstore
db.username=root
db.password=your-mysql-password
```

Phần cấu hình database hiện được đọc từ `application.properties`; vì vậy cần sửa tệp này trước khi build nếu thông tin MySQL của bạn khác mặc định.

### 4. Cấu hình email và PayOS (tùy chọn)

Email xác nhận đơn hàng và thanh toán VietQR cần các biến môi trường tương ứng. Tạo tệp `.env` từ mẫu:

```powershell
Copy-Item .env.example .env
```

Trên macOS/Linux dùng `cp .env.example .env`. Sau đó thay toàn bộ giá trị mẫu trong `.env`:

```properties
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-smtp-app-password
SMTP_FROM=your-email@gmail.com
SMTP_FROM_NAME=BOOKS
SMTP_STARTTLS=true
APP_BASE_URL=http://localhost:8080/onlinebookstore

PAYOS_CLIENT_ID=your-payos-client-id
PAYOS_API_KEY=your-payos-api-key
PAYOS_CHECKSUM_KEY=your-payos-checksum-key
PAYOS_RETURN_URL=http://localhost:8080/onlinebookstore/payment-success.jsp
PAYOS_CANCEL_URL=http://localhost:8080/onlinebookstore/checkout
```

PayOS ưu tiên biến môi trường (hoặc `.env`) hơn `application.properties`. Khi chạy trên Tomcat từ IDE mà ứng dụng không tìm thấy `.env`, đặt biến `EMAIL_ENV_FILE` thành đường dẫn tuyệt đối đến tệp `.env`, hoặc khai báo trực tiếp các biến trên trong môi trường chạy. Với môi trường production, chỉ dùng biến môi trường/secret manager; không đóng gói hoặc commit `.env`.

Để nhận webhook PayOS thật, URL triển khai phải truy cập được từ Internet và endpoint là `/onlinebookstore/api/payos-webhook`.

### 5. Build WAR

```powershell
mvn clean package
```

Lệnh thành công tạo tệp `target/onlinebookstore.war`.

> Nếu Maven báo `No compiler is provided`, máy đang dùng JRE thay vì JDK. Cài JDK 17 và đặt `JAVA_HOME` trỏ đến thư mục JDK, sau đó mở lại terminal/IDE.

### 6. Triển khai lên Tomcat

Sao chép WAR vào thư mục `webapps` của Tomcat rồi khởi động Tomcat:

```powershell
Copy-Item target/onlinebookstore.war "$env:CATALINA_HOME\webapps\"
& "$env:CATALINA_HOME\bin\startup.bat"
```

Hoặc import dự án Maven vào Eclipse/IntelliJ và deploy artifact `onlinebookstore:war` lên Tomcat 10.1+.

Mở ứng dụng tại [http://localhost:8080/onlinebookstore/](http://localhost:8080/onlinebookstore/). Nếu Tomcat dùng cổng khác, thay `8080` bằng cổng đã cấu hình.

## Tài khoản và dữ liệu thử

Script cơ sở dữ liệu chỉ tạo cấu trúc bảng, không tạo tài khoản hoặc sách mẫu. Bạn có thể đăng ký tài khoản khách hàng tại giao diện. Cột `users.usertype` dùng giá trị `2` cho khách hàng và `1` cho quản trị viên/người bán. Để cấp quyền quản trị cho một tài khoản thử đã tạo:

```sql
UPDATE users
SET usertype = 1
WHERE username = 'your-admin-username';
```

Tạo một tài khoản khách khác nếu vẫn cần kiểm thử luồng mua hàng.

## Khắc phục sự cố

| Triệu chứng | Cách xử lý |
| --- | --- |
| Không kết nối được MySQL | Kiểm tra dịch vụ MySQL đang chạy, database `onlinebookstore` đã được tạo và các khóa `db.*` trong `application.properties`. |
| Lỗi `ClassNotFoundException: jakarta.servlet...` hoặc ứng dụng không deploy | Dùng Tomcat 10.1+ và build lại bằng JDK 17. |
| Truy cập bị 404 | Xác nhận WAR đã nằm trong `webapps`, Tomcat đã giải nén nó và truy cập đúng context `/onlinebookstore/`. |
| Email không gửi | Kiểm tra biến `SMTP_*`; với Gmail cần dùng App Password, không dùng mật khẩu tài khoản thông thường. |
| Thanh toán PayOS thất bại | Kiểm tra ba biến `PAYOS_*`, URL trả về/hủy và webhook có thể truy cập công khai khi kiểm thử thanh toán thật. |

## Lưu ý bảo mật

- Không commit `.env`, mật khẩu MySQL, SMTP hoặc khóa PayOS. Tệp `.env` đã được git ignore.
- Thay mọi thông tin xác thực mẫu trước khi triển khai thật.
- Cơ chế mật khẩu hiện tại của dự án chưa phù hợp production; cần băm mật khẩu và áp dụng các biện pháp bảo mật ứng dụng trước khi đưa lên môi trường công khai.
