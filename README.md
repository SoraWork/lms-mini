# LMS Mini - RESTful API
## 0. Frontend
- [FE Repository (Vue 3 + Element Plus)](https://github.com/SoraWork/lms-mini-fe)

## 1. Mục tiêu
- Xây dựng RESTful API quản lý:
  - Học viên, khóa học, bài học, đăng ký học.
  - Upload ảnh/video cho khóa học, bài học và avatar học viên.
  - Hỗ trợ phân trang, tìm kiếm nâng cao.
- Validate dữ liệu, xử lý lỗi, hỗ trợ i18n cho thông báo.
- Export Excel danh sách khóa học và học viên.

---

## 2. Cấu trúc hệ thống

### Công nghệ sử dụng
- **Backend**: Spring Boot (Web, JPA, Validation)  
- **Validator**: Hibernate Validator (`@NotBlank`, `@Size`, `@Min`, `@Email`)  
- **Database**: MariaDB (`spring.jpa.open-in-view=false`)  
- **Frontend**: Vue 3 + Element Plus  

---

## 3. Database
![Database Schema](https://github.com/user-attachments/assets/1e55875c-fd3a-4d70-9484-f7f30eba49b9)  

---

## 4. BE API

### 4.1 Student

| Chức năng              | Endpoint | Mô tả | Hình ảnh |
|------------------------|----------|-------|---------|
| Create Student         | POST `/students` | Tạo học viên mới | ![Create Student](https://github.com/user-attachments/assets/70b6e83d-ddf8-4eb2-b945-1a1cc67badf2) |
| Search Student         | GET `/students/search` | Tìm kiếm học viên | ![Search Student](https://github.com/user-attachments/assets/24c09089-08da-4025-9776-ded112303fb4) |
| Update Student         | PUT `/students/{id}` | Cập nhật học viên | ![Update Student](https://github.com/user-attachments/assets/ef10ab92-8910-4f8b-a15c-b2b54172c8df) |
| Delete Student         | DELETE `/students/{id}` | Xóa học viên | ![Delete Student](https://github.com/user-attachments/assets/18f933fa-cf8f-46ce-a415-e4dc189e4a3b) |
| Export Student         | GET `/students/export` | Xuất Excel học viên | ![Export Student](https://github.com/user-attachments/assets/9ba0bfa2-0ead-4bb1-b66d-6ede788b8561) |

---

### 4.2 Course

| Chức năng              | Endpoint | Mô tả | Hình ảnh |
|------------------------|----------|-------|---------|
| Create Course          | POST `/courses` | Tạo khóa học mới | ![Create Course](https://github.com/user-attachments/assets/3b2d7f76-661e-495a-9600-57f1d62be9c5) |
| Update Course          | PUT `/courses/{id}` | Cập nhật khóa học | ![Update Course](https://github.com/user-attachments/assets/083ff4e8-1c2c-47b5-b9e6-fba311b1b088) |
| Search Course          | GET `/courses/search` | Tìm kiếm khóa học | ![Search Course](https://github.com/user-attachments/assets/a4532e6a-54c3-4ab5-8c5a-c84390487326) |
| Delete Course          | DELETE `/courses/{id}` | Xóa khóa học | ![Delete Course](https://github.com/user-attachments/assets/102c3145-078b-43fd-bf5e-da9468068edb) |
| Get All Lesson Of Course | GET `/courses/{id}/lessons` | Lấy danh sách bài học | ![Get All Lesson](https://github.com/user-attachments/assets/57cfac9d-2e37-4721-8001-5836c13434c4) |
| Get All Student Of Course | GET `/courses/{id}/students` | Lấy danh sách học viên | ![Get All Student](https://github.com/user-attachments/assets/1308db28-1490-4a23-8757-a17415f465fc) |
| Export Course          | GET `/courses/export` | Xuất Excel khóa học | ![Export Course](https://github.com/user-attachments/assets/a2b602aa-5066-4c95-9f16-eb8f83c33a5a) |

---

### 4.3 Lesson

| Chức năng        | Endpoint | Mô tả | Hình ảnh |
|-----------------|----------|-------|---------|
| Create Lesson    | POST `/lessons` | Tạo bài học | ![Create Lesson](https://github.com/user-attachments/assets/48b8c277-8287-41ca-a8f0-0336726780e8) |
| Update Lesson    | PUT `/lessons/{id}` | Cập nhật bài học | ![Update Lesson](https://github.com/user-attachments/assets/8cf5a506-fb5e-4e0f-91e0-e02b5dac5a18) |
| Delete Lesson    | DELETE `/lessons/{id}` | Xóa bài học | ![Delete Lesson](https://github.com/user-attachments/assets/c1d6b061-d060-4980-b1dd-37bf3d7f59e4) |

---

### 4.4 Enrollment

| Chức năng             | Endpoint | Mô tả | Hình ảnh |
|----------------------|----------|-------|---------|
| Create Enrollment    | POST `/enrollments` | Tạo đăng ký học | ![Create Enrollment](https://github.com/user-attachments/assets/c5f39f03-b254-481d-8a0f-c45a6f2c0245) |
| Update Enrollment    | PUT `/enrollments/{id}` | Cập nhật đăng ký học | ![Update Enrollment](https://github.com/user-attachments/assets/7378de6c-ba44-4fc4-925c-6d8011689b09) |
