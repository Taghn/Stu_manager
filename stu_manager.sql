-- Script khởi tạo cơ sở dữ liệu và dữ liệu mẫu cho dự án Stu_manager
-- Hệ quản trị cơ sở dữ liệu khuyên dùng: MySQL 5.7 hoặc MySQL 8.0 trở lên

CREATE DATABASE IF NOT EXISTS `Stu_manager` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `Stu_manager`;

-- 1. Tạo cấu trúc bảng `users` (Quản lý tài khoản đăng nhập)
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Chèn dữ liệu tài khoản mẫu
-- Mật khẩu mặc định là '123123' đã được băm SHA-256 một chiều
INSERT INTO `users` (`username`, `password`) VALUES
('admin', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e')
ON DUPLICATE KEY UPDATE `username`=`username`;


-- 2. Tạo cấu trúc bảng `students` (Quản lý thông tin sinh viên)
CREATE TABLE IF NOT EXISTS `students` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_id` VARCHAR(50) NOT NULL,
  `full_name` VARCHAR(100) NOT NULL,
  `class_name` VARCHAR(50) NOT NULL,
  `subject` VARCHAR(100) NULL,
  `tuition_fee` DOUBLE DEFAULT 0.0,
  UNIQUE KEY `unique_student_subject` (`student_id`, `subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Chèn danh sách sinh viên mẫu kèm Môn học và Học phí
INSERT INTO `students` (`student_id`, `full_name`, `class_name`, `subject`, `tuition_fee`) VALUES
('2305CT0001', 'Nguyễn Văn A', 'CT07PM', 'Lập trình Java', 3500000.0),
('2305CT0001', 'Nguyễn Văn A', 'CT07PM', 'Cơ sở dữ liệu', 4000000.0),
('2305CT0002', 'Trần Thị B', 'CT07PM', 'Cơ sở dữ liệu', 4000000.0),
('2305CT0003', 'Lê Hoàng C', 'CT08PM', 'Phân tích thiết kế hệ thống', 3800000.0),
('2305CT0004', 'Phạm Minh D', 'CT07PM', 'Lập trình Web nâng cao', 4500000.0),
('2305CT0005', 'Vũ Hoài E', 'CT08PM', 'An toàn thông tin', 3600000.0)
ON DUPLICATE KEY UPDATE `student_id`=`student_id`;
