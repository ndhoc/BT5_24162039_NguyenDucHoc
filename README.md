# BT5 Spring Boot 4 – Nguyễn Đức Học (24162039)

BT5 dùng Spring Boot 4.0.10, Spring MVC, Java 21 và SQL Server. Các chức năng cũ của BT2 đã được chuyển sang Spring MVC controller, giữ đường dẫn và giao diện JSP/JSTL. Category và User quản lý bằng Spring JDBC. Sáu repository JPA là interface do Spring Data tạo proxy; các truy vấn đặc thù hiện vẫn dùng `JpaConfig` trong default method để giữ nguyên hành vi. Service interface và lớp triển khai nằm trong `service` và `service/impl`.

Toàn bộ mã Java nằm dưới package `com.nguyenduchoc.bt5` và các package con như `controllers`, `entity`, `repository`, `service`.

## Chức năng

- Trang chủ, sản phẩm, video và upload video, dashboard, hồ sơ, OTP kích hoạt/quên mật khẩu, đăng ký, đăng nhập, đăng xuất, cookie ghi nhớ và quản trị sản phẩm/video từ BT2.
- `/admin/category`: admin thêm, sửa, xóa, tìm kiếm và phân trang Category (10 mục/trang).
- `/admin/user`: admin thêm, sửa, xóa, tìm theo username/họ tên/email/số điện thoại và phân trang User (10 mục/trang).
- CSRF cho POST và kiểm tra vai trò admin. Không thể tự xóa, khóa hoặc hạ quyền tài khoản admin đang dùng.

## Chạy

Yêu cầu JDK 21, Maven và SQL Server. Chạy `database/schema.sql`, rồi `database/seed.sql` bằng `sqlcmd -f 65001`; tài khoản mẫu: `admin / 123456`.

Đặt `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` nếu không dùng cấu hình mặc định. Chúng áp dụng cho Spring JDBC, Spring Data JPA và các truy vấn JPA đặc thù. Chạy `mvn clean package`, sau đó `java -jar target/BT5_24162039_NguyenDucHoc-1.0.0.war`, hoặc deploy WAR lên Tomcat 11. Mở `http://localhost:8080/` khi chạy WAR trực tiếp.

Cấu hình SMTP và Cloudinary nằm trong `src/main/resources/application.properties`. Đặt các biến môi trường `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_AUTH`, `MAIL_STARTTLS`, `MAIL_TIMEOUT` và `CLOUDINARY_URL` khi chạy; không ghi mật khẩu hoặc API secret vào mã nguồn. Nếu `MAIL_HOST` trống, OTP được ghi vào log cục bộ. Nếu `CLOUDINARY_URL` trống, chức năng tải ảnh đại diện sẽ báo chưa cấu hình. Biểu mẫu Category hỗ trợ ảnh PNG/JPEG hoặc đường dẫn ảnh.

Project không còn lớp `@WebServlet`, `@WebFilter` hay `@WebListener`. Spring MVC xử lý request và `MultipartFile` xử lý upload. Mã nguồn JSP/tag nằm trong `src/main/resources/META-INF/WEB-INF`; Maven đưa chúng vào `/WEB-INF` của WAR và sao chép vào classpath khi chạy từ source để Tomcat biên dịch JSP.

Lớp khởi động là `BT5_24162039_NguyenDucHocApplication.java`, cùng tên project. Mã còn lại nằm trong các package `controllers`, `config`, `dto`, `jdbc`, `validation`, `service`, `repository` và `utils`.

DevTools vẫn là dependency phát triển, nhưng tự khởi động lại được tắt mặc định khi chạy từ source vì `JpaConfig` cũ không tương thích với restart classloader. Có thể bật lại bằng JVM property `-Dspring.devtools.restart.enabled=true` sau khi chuyển hết truy vấn cũ sang Spring Data JPA.

## Đưa lên GitHub

`src/main/resources/application.properties` và `src/main/resources/META-INF/persistence.xml` chứa cấu hình riêng trên máy nên được `.gitignore` bỏ qua. Commit hai file `.example` cùng vị trí. Sau khi clone, sao chép `application.properties.example` thành `application.properties` và `persistence.xml.example` thành `persistence.xml`, rồi đặt biến môi trường `DB_PASSWORD` (và `DB_URL`, `DB_USERNAME` nếu khác mặc định). `MAIL_PASSWORD` và `CLOUDINARY_URL` cũng đặt bằng biến môi trường khi dùng dịch vụ thật. Không commit mật khẩu hoặc API secret.