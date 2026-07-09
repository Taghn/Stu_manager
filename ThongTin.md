# Thông Tin Dự Án: Stu_manager (Cập nhật)

Dự án quản lý sinh viên chạy trên máy chủ Tomcat, sử dụng cơ sở dữ liệu **MySQL** và tích hợp trang đăng nhập bảo mật băm mật khẩu một chiều.

---

## 1. Công Nghệ Sử Dụng (Tech Stack)

* **Frontend**: HTML5, CSS3 (Giao diện sáng Premium), JavaScript thuần (Fetch API).
* **Backend**: Java Servlet (Java EE 8 / Servlet 4.0.1).
* **Database**: MySQL Server (Kết nối tự động và tự động tạo cơ sở dữ liệu/bảng nếu chưa có).
* **JSON Parser**: Google Gson (Dùng chuyển đổi dữ liệu qua lại giữa Client - Server).
* **Build Tool**: Apache Maven (Tích hợp sẵn Maven Wrapper `mvnw`).
* **Web Server**: Apache Tomcat (Khuyên dùng Tomcat 9 trở lên).

---

## 3. Chức Năng Đăng Nhập & Bảo Mật

* **Mật khẩu băm một chiều:** Hệ thống sử dụng thuật toán **SHA-256** bảo mật được tích hợp sẵn ở Servlet. Khi tài khoản được tạo hoặc xác thực, mật khẩu sẽ được băm và so sánh ở dạng chuỗi băm (Hash).
* **Tài khoản mặc định (Được tự động khởi tạo khi bảng trống):**
  * **Tên đăng nhập (Username):** `admin`
  * **Mật khẩu (Password):** `123123`
* **Quản lý phiên làm việc (Session):**
  * Thời gian hết hạn của phiên làm việc khi không hoạt động (Session Timeout) được cấu hình là **10 phút**.
  * Cấu hình đồng bộ trong tệp tin `web.xml` và lập trình trong `AuthServlet.java` khi người dùng đăng nhập thành công.
* **Bảo vệ API:** Toàn bộ các API phục vụ Thêm, Sửa, Xóa, Xem danh sách sinh viên tại `StudentServlet` đều được kiểm tra session. Nếu chưa đăng nhập, Server lập tức chặn truy cập và trả về mã lỗi `401 Unauthorized`.

---

## 4. Thiết Kế Giao Diện & Trải Nghiệm (UX/UI)

* **Luồng điều hướng tuần tự:** 
  * Giao diện loại bỏ hoàn toàn các Tab song song trên thanh Header.
  * Sau khi đăng nhập, trang mặc định là **Danh sách sinh viên**.
  * Cung cấp nút **Thêm sinh viên** ở phía trên bảng dữ liệu để di chuyển sang trang điền thông tin.
  * Form nhập thông tin cung cấp thêm nút **Quay lại danh sách** và **Hủy bỏ** giúp chuyển hướng nhanh chóng.
  * Sau khi Thêm sinh viên thành công, ứng dụng tự động chuyển hướng người dùng về lại trang danh sách.
* **Thông báo nổi (Toast):**
  * Hiển thị thông báo khi thực hiện thao tác (Đăng nhập, Thêm, Sửa, Xóa thành công hoặc có lỗi).
  * Thời gian hiển thị được cấu hình tối ưu từ **2.5 giây** rồi tự động tắt và xóa khỏi DOM để tránh lỗi chồng đống thông báo.

---

## 5. Hướng Dẫn Biên Dịch & Đóng Gói (Build WAR)

Để đóng gói ứng dụng thành file `.war` đưa lên Tomcat, bạn hãy mở **PowerShell** tại thư mục dự án và chạy lệnh sau:

```powershell
# Di chuyển đến thư mục dự án (nếu chưa ở trong thư mục)
cd C:\Users\suthe\Desktop\Stu_manager

# Thực hiện biên dịch và đóng gói bằng Maven Wrapper
.\mvnw.cmd clean package
```

