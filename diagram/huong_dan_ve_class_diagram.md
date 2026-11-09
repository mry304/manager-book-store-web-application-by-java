# Hướng dẫn vẽ UML Class Diagram (Biểu đồ lớp)

## 1. Class Diagram là gì?

**Class Diagram (biểu đồ lớp)** là một biểu đồ UML mô tả cấu trúc tĩnh của hệ thống hướng đối tượng. Nó cho biết hệ thống có những lớp nào, mỗi lớp lưu trữ dữ liệu gì, cung cấp những hành vi nào và các lớp liên hệ với nhau ra sao.

Một Class Diagram thường thể hiện:

- **Class (lớp):** khuôn mẫu mô tả một nhóm đối tượng có chung dữ liệu và hành vi.
- **Attribute (thuộc tính):** dữ liệu mà lớp lưu trữ.
- **Operation/Method (phương thức):** hành vi mà lớp cung cấp.
- **Relationship (mối quan hệ):** cách các lớp liên kết, kế thừa hoặc phụ thuộc lẫn nhau.
- **Multiplicity (bội số):** số lượng đối tượng có thể tham gia vào một mối quan hệ.

Class Diagram tập trung vào cấu trúc, không mô tả thứ tự các bước xử lý như Activity Diagram hay Sequence Diagram.

---

## 2. Cách vẽ một lớp (Class)

### 2.1. Cấu trúc hình chữ nhật

Một lớp thường được biểu diễn bằng hình chữ nhật chia thành ba ngăn:

```text
+-----------------------------+
|          Person             |  <- Tên lớp
+-----------------------------+
| - id: int                   |  <- Thuộc tính
| - name: string              |
| - age: int                  |
+-----------------------------+
| + speak(): void             |  <- Phương thức
| # run(): void               |
+-----------------------------+
```

Ý nghĩa từng ngăn:

1. **Ngăn trên cùng – Tên lớp:** ghi tên lớp, thường dùng danh từ và viết theo PascalCase, ví dụ `Person`, `Student`, `Book`, `Order`.
2. **Ngăn giữa – Thuộc tính:** ghi các dữ liệu mà đối tượng của lớp cần lưu trữ.
3. **Ngăn dưới – Phương thức:** ghi các hành vi hoặc chức năng mà lớp cung cấp.

Có thể chỉ vẽ tên lớp khi chưa cần mô tả chi tiết thuộc tính và phương thức. Không nhất thiết lớp nào cũng phải có đủ cả ba ngăn.

### 2.2. Cách ghi thuộc tính

Cú pháp phổ biến:

```text
visibility name: Type
```

Ví dụ:

```text
- id: int
- fullName: string
- dateOfBirth: Date
- salary: decimal
```

Trong đó:

- `visibility`: phạm vi truy cập của thuộc tính.
- `name`: tên thuộc tính.
- `Type`: kiểu dữ liệu, chẳng hạn `int`, `string`, `boolean`, `Date`, `decimal` hoặc một lớp do hệ thống định nghĩa.

Một số ký hiệu phạm vi truy cập:

| Ký hiệu | Ý nghĩa | Giải thích |
|---|---|---|
| `+` | Public | Có thể được truy cập từ bên ngoài lớp. |
| `-` | Private | Chỉ có thể được truy cập trực tiếp bên trong lớp. |
| `#` | Protected | Có thể được truy cập trong lớp và các lớp kế thừa, theo quy tắc của ngôn ngữ lập trình. |
| `~` | Package | Có thể được truy cập trong cùng phạm vi package. |

Ví dụ `- id: int` nghĩa là thuộc tính `id` có phạm vi private và kiểu dữ liệu là số nguyên.

### 2.3. Cách ghi phương thức

Cú pháp phổ biến:

```text
visibility methodName(parameterName: Type): ReturnType
```

Ví dụ:

```text
+ getName(): string
+ updateProfile(newName: string): void
- calculateTotal(): decimal
# validateData(): boolean
```

Cách đọc:

- `getName()` là phương thức public, không nhận tham số và trả về `string`.
- `updateProfile(newName: string)` nhận tham số `newName` kiểu `string` và không trả về giá trị (`void`).
- `calculateTotal()` là phương thức private, trả về `decimal`.

Nếu phương thức không nhận tham số, để trống phần trong ngoặc. Nếu phương thức không trả về giá trị, ghi `void` sau dấu hai chấm.

### 2.4. Lưu ý khi chọn kiểu dữ liệu

Kiểu dữ liệu phải phù hợp với ý nghĩa của thuộc tính. Ví dụ, `age` thường nên là `int` thay vì `string`; `isActive` thường là `boolean`; `price` có thể là `decimal`. Không nên ghi tất cả thuộc tính thành `string` chỉ để đơn giản hóa sơ đồ.

---

## 3. Sáu kiểu quan hệ thường gặp

Hình minh họa thứ hai tổng hợp sáu kiểu quan hệ quan trọng. Khi vẽ, cần chú ý cả **kiểu đường nối**, **đầu mũi tên** và **vị trí hình thoi**.

### 3.1. Association – Quan hệ liên kết

**Ký hiệu:** đường thẳng liền nối hai lớp. Có thể có tên quan hệ, vai trò ở hai đầu hoặc mũi tên chỉ hướng điều hướng.

Ví dụ:

```text
Teacher ---------------- Student
             teaches
```

Ý nghĩa: hai lớp có liên hệ với nhau. Chẳng hạn, giáo viên dạy học sinh; khách hàng đặt đơn hàng; nhân viên xử lý yêu cầu.

Cách sử dụng:

- Dùng khi một đối tượng có liên hệ hoặc tương tác với đối tượng thuộc lớp khác.
- Có thể ghi tên quan hệ trên đường nối, thường dùng cụm động từ như `places`, `contains`, `teaches`, `works for`.
- Có thể ghi bội số ở hai đầu đường nối.
- Đường nối không có mũi tên thường không chỉ rõ hướng điều hướng. Không nên tự suy luận rằng mọi association đều bắt buộc phải là hai chiều; hãy thể hiện hướng khi mô hình cần làm rõ điều đó.

### 3.2. Inheritance / Generalization – Quan hệ kế thừa

**Ký hiệu:** đường thẳng liền với đầu tam giác rỗng, tam giác hướng về lớp cha.

Ví dụ:

```text
          Person
             △
             |
       -------------
       |           |
    Teacher     Student
```

Ý nghĩa: lớp con kế thừa các thuộc tính và phương thức phù hợp từ lớp cha.

Trong ví dụ, `Teacher` và `Student` là các loại `Person`. Vì vậy, thông tin chung như `id`, `name` hoặc hành vi chung có thể đặt trong `Person`; mỗi lớp con bổ sung thông tin riêng.

Cách sử dụng:

- Chỉ dùng khi có quan hệ **“là một loại của” (is-a)**.
- Đầu tam giác rỗng luôn hướng về lớp tổng quát/lớp cha.
- Không dùng kế thừa chỉ vì hai lớp có một vài thuộc tính giống nhau.

### 3.3. Realization – Quan hệ hiện thực hóa

**Ký hiệu:** đường nét đứt với đầu tam giác rỗng, tam giác hướng về interface.

Ví dụ:

```text
        <<interface>>
          Payable
             △
             |
       - - - - - - -
             |
          Invoice
```

Ý nghĩa: một lớp hiện thực hóa (implements) một interface, tức là lớp đó cung cấp phần cài đặt cho các hành vi được quy định bởi interface.

Cách sử dụng:

- Dùng khi mô hình có interface hoặc hợp đồng hành vi.
- Đầu tam giác rỗng hướng về interface.
- Phân biệt với kế thừa: **kế thừa dùng đường liền**, còn **hiện thực hóa dùng đường nét đứt**.

### 3.4. Dependency – Quan hệ phụ thuộc

**Ký hiệu:** đường nét đứt có đầu mũi tên thường, hướng từ lớp phụ thuộc đến lớp được sử dụng.

Ví dụ:

```text
OrderService - - - - - > EmailService
```

Ý nghĩa: `OrderService` phụ thuộc vào `EmailService`, chẳng hạn gọi phương thức gửi email trong quá trình xử lý đơn hàng.

Cách sử dụng:

- Dùng khi một lớp sử dụng lớp khác tạm thời, ví dụ thông qua tham số phương thức, biến cục bộ hoặc lời gọi phương thức.
- Mũi tên hướng đến lớp được sử dụng.
- Dependency thường thể hiện mức liên hệ lỏng hơn association.

### 3.5. Aggregation – Quan hệ kết tập

**Ký hiệu:** đường thẳng liền có hình thoi rỗng ở đầu phía **tổng thể (whole)**.

Ví dụ:

```text
Department ◇---------------- Teacher
```

Ý nghĩa: mô tả quan hệ tổng thể – thành phần nhưng thành phần vẫn có thể tồn tại độc lập với tổng thể.

Ví dụ thường dùng: một phòng ban có các nhân viên; khi phòng ban thay đổi hoặc bị xóa, nhân viên không nhất thiết bị xóa theo.

Cách sử dụng:

- Hình thoi rỗng đặt ở phía đối tượng đóng vai trò tổng thể.
- Đầu còn lại là thành phần.
- Chỉ dùng khi ý nghĩa tổng thể – thành phần thực sự cần thiết; nếu chỉ là liên kết thông thường thì association thường đã đủ.

### 3.6. Composition – Quan hệ hợp thành

**Ký hiệu:** đường thẳng liền có hình thoi đen ở đầu phía **tổng thể (whole)**.

Ví dụ:

```text
School ◆---------------- Toilet
```

Ý nghĩa: mô tả quan hệ tổng thể – thành phần có sự phụ thuộc chặt chẽ về vòng đời. Thành phần thuộc về một tổng thể tại một thời điểm và thường không có ý nghĩa tồn tại độc lập ngoài tổng thể đó.

Ví dụ minh họa: một `School` quản lý các `Toilet` như những bộ phận cấu thành của trường. Nếu trường bị xóa trong mô hình vòng đời này, các nhà vệ sinh thuộc trường cũng bị xóa theo.

Cách sử dụng:

- Hình thoi đen đặt ở phía tổng thể.
- Đầu còn lại là thành phần.
- Chỉ chọn composition khi vòng đời của thành phần thực sự phụ thuộc vào tổng thể; không nên dùng hình thoi đen chỉ vì một đối tượng “có” đối tượng khác.

### 3.7. Bảng so sánh nhanh

| Quan hệ | Đường nối | Ký hiệu ở đầu | Hướng đọc / ý nghĩa |
|---|---|---|---|
| Association | Liền | Không bắt buộc có mũi tên | Hai lớp có liên hệ. |
| Inheritance | Liền | Tam giác rỗng | Lớp con → lớp cha; tam giác hướng về lớp cha. |
| Realization | Nét đứt | Tam giác rỗng | Lớp triển khai → interface; tam giác hướng về interface. |
| Dependency | Nét đứt | Mũi tên thường | Lớp phụ thuộc → lớp được sử dụng. |
| Aggregation | Liền | Hình thoi rỗng | Hình thoi ở phía tổng thể; thành phần có thể tồn tại độc lập. |
| Composition | Liền | Hình thoi đen | Hình thoi ở phía tổng thể; vòng đời thành phần phụ thuộc tổng thể. |

**Mẹo phân biệt nhanh:** Inheritance và Realization đều có tam giác rỗng, nhưng khác kiểu đường nối; Aggregation và Composition đều có hình thoi, nhưng khác hình thoi rỗng/đen.

---

## 4. Multiplicity – Bội số trong quan hệ

Multiplicity cho biết một đối tượng ở đầu này có thể liên kết với bao nhiêu đối tượng ở đầu còn lại. Bội số được ghi gần đầu đường nối tương ứng.

### 4.1. Các ký hiệu thường gặp

| Ký hiệu | Ý nghĩa |
|---|---|
| `1` | Chính xác một. |
| `0..1` | Không có hoặc có tối đa một. |
| `*` hoặc `0..*` | Không có hoặc có nhiều. |
| `1..*` | Ít nhất một. |
| `2..5` | Từ hai đến năm. |
| `m..n` | Từ m đến n. |

### 4.2. Cách đọc bội số đúng

Xét quan hệ `School` – `Student`:

```text
School 1 ---------------- 1..* Student
```

Giả sử `1` được ghi gần đầu `School`, còn `1..*` được ghi gần đầu `Student`. Ta đọc như sau:

- Với một `School`, có từ một đến nhiều `Student`.
- Mỗi `Student` liên kết với đúng một `School`.

**Quy tắc quan trọng:** bội số đặt gần đầu lớp nào thì mô tả số lượng đối tượng của lớp đó có thể liên kết với một đối tượng ở đầu đối diện. Đừng đọc bội số ở đầu `School` thành số lượng học sinh; nó mô tả số lượng trường tương ứng với một học sinh.

### 4.3. Một số ví dụ khác

**Khách hàng – Đơn hàng**

```text
Customer 1 ---------------- 0..* Order
```

Một khách hàng có thể chưa có đơn hàng hoặc có nhiều đơn hàng; mỗi đơn hàng thuộc về đúng một khách hàng.

**Sinh viên – Lớp học**

```text
Student 0..* ---------------- 0..* Course
```

Một sinh viên có thể đăng ký nhiều môn học; một môn học có thể có nhiều sinh viên. Đây là quan hệ nhiều – nhiều.

**Đơn hàng – Chi tiết đơn hàng**

```text
Order 1 ◆---------------- 1..* OrderDetail
```

Một đơn hàng có ít nhất một chi tiết đơn hàng; mỗi chi tiết đơn hàng thuộc về đúng một đơn hàng. Hình thoi đen đặt ở phía `Order` nếu chọn mô hình composition.

---

## 5. Quy trình xây dựng Class Diagram từ đề bài

Nên thực hiện theo thứ tự dưới đây thay vì vẽ các đường nối ngay từ đầu.

### Bước 1: Đọc yêu cầu và xác định các danh từ quan trọng

Đọc mô tả hệ thống, gạch chân các danh từ đại diện cho thực thể hoặc khái niệm cần quản lý.

Ví dụ với hệ thống bán sách:

- `Book` – Sách
- `Customer` – Khách hàng
- `Order` – Đơn hàng
- `OrderDetail` – Chi tiết đơn hàng
- `Payment` – Thanh toán
- `Category` – Danh mục

Không phải danh từ nào trong đề bài cũng cần trở thành một lớp. Chỉ chọn những khái niệm có dữ liệu, hành vi hoặc vai trò riêng trong hệ thống.

### Bước 2: Xác định thuộc tính của từng lớp

Với mỗi lớp, đặt câu hỏi: **Hệ thống cần lưu thông tin gì về đối tượng này?**

Ví dụ:

```text
Book
-------------------------
- bookId: int
- title: string
- author: string
- price: decimal
- quantity: int
```

Chọn thuộc tính có ý nghĩa với phạm vi bài toán. Tránh đưa dữ liệu trùng lặp hoặc thông tin không cần quản lý vào lớp.

### Bước 3: Xác định phương thức

Đặt câu hỏi: **Đối tượng này cần thực hiện những hành vi nào?**

Ví dụ:

```text
Order
-------------------------
- orderId: int
- orderDate: Date
- totalAmount: decimal
-------------------------
+ calculateTotal(): decimal
+ cancel(): void
```

Không nhất thiết liệt kê mọi getter/setter nếu giảng viên hoặc yêu cầu đồ án không cần. Ưu tiên các hành vi thể hiện nghiệp vụ của lớp.

### Bước 4: Xác định quan hệ giữa các lớp

Tìm các động từ hoặc cụm từ thể hiện liên hệ trong mô tả nghiệp vụ:

- Khách hàng **đặt** đơn hàng → `Customer` liên kết với `Order`.
- Đơn hàng **bao gồm** các chi tiết đơn hàng → có thể là composition giữa `Order` và `OrderDetail`.
- Sinh viên **là một loại** người dùng → có thể là inheritance nếu các lớp thực sự chia sẻ đặc điểm chung và quan hệ “is-a” hợp lý.
- Một lớp **gọi chức năng** của lớp dịch vụ khác → có thể thể hiện dependency nếu chỉ là sử dụng tạm thời.

Chọn loại quan hệ dựa trên ý nghĩa nghiệp vụ, không chọn chỉ vì hình vẽ trông phù hợp.

### Bước 5: Xác định bội số

Với mỗi quan hệ, trả lời hai câu hỏi:

1. Một đối tượng của lớp A có thể liên kết với bao nhiêu đối tượng của lớp B?
2. Một đối tượng của lớp B có thể liên kết với bao nhiêu đối tượng của lớp A?

Từ câu trả lời, ghi bội số ở đúng đầu đường nối. Nếu chưa chắc chắn, quay lại yêu cầu nghiệp vụ để kiểm tra quy tắc.

### Bước 6: Sắp xếp và hoàn thiện sơ đồ

- Đặt lớp tổng quát ở phía trên, các lớp kế thừa ở phía dưới.
- Đặt các lớp trung tâm gần giữa sơ đồ; các lớp liên quan xung quanh.
- Hạn chế đường nối chồng chéo và giao cắt không cần thiết.
- Ghi tên quan hệ khi tên đó giúp làm rõ ý nghĩa.
- Kiểm tra lại hướng tam giác và vị trí hình thoi.
- Kiểm tra bội số ở cả hai đầu.

---

## 6. Phân tích sơ đồ minh họa trong hình

Hình đầu tiên có các lớp `Person`, `Teacher`, `Student`, `School` và `Toilet`.

### 6.1. Quan hệ kế thừa

- `Person` nằm ở phía trên và đóng vai trò lớp cha.
- `Teacher` và `Student` nằm phía dưới, có đường liền với tam giác rỗng hướng lên `Person`.
- Có thể hiểu `Teacher` và `Student` là hai loại `Person`.

Các thông tin dùng chung như mã định danh, tên hoặc tuổi có thể đặt ở `Person`; mỗi lớp con bổ sung thông tin riêng.

### 6.2. Quan hệ liên kết

Đường thẳng nối `Teacher` và `Student` là association. Đường nối có thể biểu diễn một quan hệ giữa giáo viên và học sinh; cần bổ sung tên quan hệ và bội số nếu yêu cầu nghiệp vụ cần làm rõ.

Các đường nối chéo có nhãn `Work on` và `Study at` thể hiện quan hệ giữa các lớp với `School`. Khi đọc sơ đồ, cần nhìn vị trí ký hiệu ở từng đầu để xác định kiểu quan hệ và đọc bội số ở đúng đầu.

**Lưu ý:** hình thoi rỗng trong UML phải nằm ở phía đóng vai trò tổng thể của aggregation. Vì vậy, hãy xác nhận lớp nào thực sự là tổng thể trong nghiệp vụ rồi mới đặt hình thoi; không nên đặt theo cảm tính hoặc chỉ dựa vào tên đường nối.

### 6.3. Quan hệ composition

Giữa `School` và `Toilet` có đường nối với hình thoi đen ở đầu `School`:

- `School` là phía tổng thể.
- `Toilet` là phía thành phần.
- Bội số minh họa cho thấy một trường có thể có nhiều nhà vệ sinh và mỗi nhà vệ sinh thuộc về một trường.

Đây là ví dụ trực quan về composition. Khi áp dụng vào hệ thống thực tế, cần bảo đảm quy tắc vòng đời của thành phần phù hợp với nghiệp vụ.

### 6.4. Thuộc tính và phương thức

Các lớp trong hình sử dụng dấu `-` trước thuộc tính và dấu `+` hoặc `#` trước phương thức. Có thể đọc như sau:

```text
- Id: int       // thuộc tính private
- Name: string  // thuộc tính private
# Run(): void   // phương thức protected
+ Speak(): void // phương thức public
```

Các dòng `field: type` và `method(type): type` ở `Teacher`, `Student`, `School`, `Toilet` là phần giữ chỗ. Khi tự thiết kế, cần thay bằng tên thuộc tính, tham số, kiểu dữ liệu và kiểu trả về cụ thể.

---

## 7. Những lỗi thường gặp khi vẽ Class Diagram

1. **Nhầm kế thừa với association:** nếu lớp B là một loại của lớp A thì cân nhắc inheritance; nếu hai lớp chỉ có liên hệ nghiệp vụ thì dùng association.
2. **Đặt sai hướng tam giác:** tam giác rỗng của inheritance/realization phải hướng về lớp cha/interface.
3. **Nhầm đường nét đứt:** realization có tam giác rỗng; dependency có mũi tên thường.
4. **Đặt sai hình thoi:** hình thoi của aggregation/composition luôn nằm ở phía tổng thể, không phải phía thành phần.
5. **Nhầm aggregation với composition:** composition hàm ý ràng buộc vòng đời mạnh hơn; aggregation chỉ thể hiện quan hệ tổng thể – thành phần yếu hơn.
6. **Bỏ quên multiplicity:** thiếu bội số khiến người đọc không biết quan hệ là một-một, một-nhiều hay nhiều-nhiều.
7. **Đọc ngược multiplicity:** bội số gần lớp nào mô tả số đối tượng của lớp đó đối với một đối tượng ở đầu bên kia.
8. **Dùng sai kiểu dữ liệu:** ví dụ tuổi là `int`, trạng thái đúng/sai là `boolean`, giá tiền thường là `decimal`.
9. **Đưa quá nhiều chi tiết vào sơ đồ:** chỉ giữ những lớp, thuộc tính và phương thức cần thiết cho mục tiêu mô hình hóa.
10. **Dùng tên lớp không nhất quán:** nên thống nhất cách viết, ví dụ `OrderDetail` thay vì lúc ghi `Order_Detail`, lúc ghi `OrderDetails`.

---

## 8. Checklist kiểm tra trước khi nộp

- [ ] Mỗi lớp có tên rõ ràng và đại diện cho một khái niệm trong hệ thống.
- [ ] Thuộc tính có tên, kiểu dữ liệu và phạm vi truy cập phù hợp.
- [ ] Phương thức có tên, tham số, kiểu trả về và phạm vi truy cập hợp lý.
- [ ] Quan hệ giữa các lớp phản ánh đúng nghiệp vụ.
- [ ] Tam giác kế thừa/hiện thực hóa hướng về đúng lớp cha/interface.
- [ ] Mũi tên dependency hướng về lớp được sử dụng.
- [ ] Hình thoi aggregation/composition nằm ở phía tổng thể.
- [ ] Mỗi quan hệ cần thiết đều có multiplicity ở đúng hai đầu.
- [ ] Tên quan hệ và bội số không mâu thuẫn với nhau.
- [ ] Sơ đồ dễ đọc, đường nối ít giao cắt và không có ký hiệu thừa.

---

## 9. Tóm tắt cách nhớ nhanh

- **Hình chữ nhật chia ngăn:** một lớp, gồm tên lớp, thuộc tính và phương thức.
- **`+`, `-`, `#`, `~`:** public, private, protected, package.
- **Đường liền + tam giác rỗng:** kế thừa; tam giác chỉ về lớp cha.
- **Đường nét đứt + tam giác rỗng:** hiện thực hóa; tam giác chỉ về interface.
- **Đường nét đứt + mũi tên thường:** phụ thuộc; mũi tên chỉ về lớp được sử dụng.
- **Đường liền + hình thoi rỗng:** aggregation.
- **Đường liền + hình thoi đen:** composition.
- **`1`, `0..1`, `*`, `1..*`:** số lượng đối tượng tham gia quan hệ.

Trình tự nên nhớ khi làm bài: **xác định lớp → xác định thuộc tính/phương thức → xác định quan hệ → xác định bội số → kiểm tra và sắp xếp sơ đồ.**
