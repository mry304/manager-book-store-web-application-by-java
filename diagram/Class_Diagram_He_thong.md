# Hình 3.1. Sơ đồ lớp nghiệp vụ của hệ thống quản lý mua bán sách

![Sơ đồ lớp nghiệp vụ của hệ thống](Class_Diagram_He_thong.svg)

Sơ đồ mô tả các lớp miền nghiệp vụ trung tâm, là mức trừu tượng phù hợp để trình bày trong báo cáo. Những lớp mang nhãn `«planned»` là thành phần thiết kế cần bổ sung cho sản phẩm của học phần hiện tại. Các lớp `User`, `Book`, `Cart`, `Order`, `OrderDetail`, `UserRole` và `OrderStatus` đối chiếu với cấu trúc mã nguồn hiện có; các thuộc tính mở rộng của `Book`, trạng thái `EXPIRED` và những thành phần còn lại là thiết kế hoàn thiện.

```mermaid
classDiagram
direction TB

class User {
  -String username
  -String emailId
  -String password
  -String firstName
  -String lastName
  -Long phone
  -String address
}
class UserRole {
  <<enumeration>>
  CUSTOMER
  SELLER_ADMIN
}
class Cart {
  -int quantity
  +changeQuantity(quantity: int) void
}
class Category {
  <<planned>>
  -Long id
  -String name
  -boolean active
}
class Book {
  -String barcode
  -String name
  -String author
  -decimal price
  -int quantity
  -String publisher
  -String imageUrl
  -BookStatus status
}
class Order {
  -String orderId
  -Long orderCode
  -Date orderDate
  -decimal totalAmount
  -OrderStatus status
  -String shippingEmail
  -String shippingAddress
  -Date shippedAt
  +cancel(reason: String) void
  +updateStatus(status: OrderStatus) void
}
class OrderDetail {
  -Long id
  -int quantity
  -decimal unitPrice
  -decimal amount
}
class Payment {
  <<planned>>
  -Long id
  -String providerTransactionId
  -PaymentStatus status
  -String checkoutUrl
  -String qrCode
  -Date paidAt
  +verifyWebhookSignature() boolean
}
class UsedBookListing {
  <<planned>>
  -Long id
  -String bookTitle
  -String author
  -decimal originalPrice
  -decimal resalePrice
  -int conditionPercent
  -UsedBookStatus status
  +review() void
}
class BookCoinAccount {
  <<planned>>
  -Long id
  -int balance
  +credit(amount: int) void
}
class BookCoinTransaction {
  <<planned>>
  -Long id
  -int amount
  -String reason
  -Date createdAt
}
class Notification {
  <<planned>>
  -Long id
  -String type
  -String content
  -boolean isRead
  -Date createdAt
  +markAsRead() void
}
class AdminAuditLog {
  <<planned>>
  -Long id
  -String action
  -String entityType
  -String entityId
  -Date createdAt
}
class BookStatus {
  <<enumeration>>
  ACTIVE
  INACTIVE
}
class OrderStatus {
  <<enumeration>>
  PENDING
  CONFIRMED
  SHIPPING
  COMPLETED
  CANCELLED
  EXPIRED
}
class PaymentStatus {
  <<enumeration>>
  PENDING
  SUCCESS
  FAILED
  EXPIRED
}
class UsedBookStatus {
  <<enumeration>>
  PENDING
  NEED_MORE_INFO
  APPROVED
  REJECTED
  SOLD
}

User "1" --> "1..*" UserRole : is assigned
User "1" --> "0..*" Cart : owns
Cart "0..*" --> "1" Book : stores
Category "1" --> "0..*" Book : classifies
User "1" --> "0..*" Order : places
Order "1" *-- "1..*" OrderDetail : consists of
OrderDetail "0..*" --> "1" Book : refers to
Order "1" *-- "0..1" Payment : has
User "1" --> "0..*" UsedBookListing : consigns
User "1" --> "0..1" BookCoinAccount : owns
BookCoinAccount "1" *-- "0..*" BookCoinTransaction : records
UsedBookListing "0..1" ..> BookCoinTransaction : creates when SOLD
User "1" --> "0..*" Notification : receives
User "1" --> "0..*" AdminAuditLog : performs
```

**Chú giải:** đường liền biểu thị association; hình thoi đen biểu thị composition; đường nét đứt biểu thị dependency. Bội số được ghi sát đầu lớp mà nó mô tả.
