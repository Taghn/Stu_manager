package com.stumanager.controller;

import com.google.gson.Gson;
import com.stumanager.dao.StudentDAO;
import com.stumanager.model.Student;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/students")
public class StudentServlet extends HttpServlet {
    private final StudentDAO studentDAO = new StudentDAO();
    private final Gson gson = new Gson();

    // Helper to verify session login status
    private boolean checkSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Vui lòng đăng nhập hệ thống trước!");
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, err);
            return false;
        }
        return true;
    }

    // Helper to send JSON responses
    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        
        try (PrintWriter out = response.getWriter()) {
            out.print(this.gson.toJson(data));
            out.flush();
        }
    }

    // Helper to read JSON request body
    private <T> T readJsonBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        request.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), clazz);
    }

    // 1. GET: Fetch all students
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!checkSession(request, response)) return;
        List<Student> list = studentDAO.getAllStudents();
        sendJsonResponse(response, HttpServletResponse.SC_OK, list);
    }

    // 2. POST: Create a new student
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!checkSession(request, response)) return;
        try {
            Student student = readJsonBody(request, Student.class);
            
            if (student == null || student.getStudentId() == null || student.getFullName() == null) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Dữ liệu không hợp lệ.");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            // Check duplicate MSSV + subject
            if (studentDAO.isStudentSubjectExists(student.getStudentId(), student.getSubject(), null)) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Sinh viên có MSSV này đã đăng ký môn học này rồi!");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            // Check if studentId is used by another student (different name/class)
            if (studentDAO.isStudentIdMismatch(student.getStudentId(), student.getFullName(), student.getClassName(), null)) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Mã số sinh viên (MSSV) này đã được đăng ký cho sinh viên khác!");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            // Check if this student (name/class) is registered under another studentId
            if (studentDAO.isStudentIdentityMismatch(student.getStudentId(), student.getFullName(), student.getClassName(), null)) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Sinh viên này đã có mã số sinh viên (MSSV) khác trong hệ thống!");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            boolean success = studentDAO.addStudent(student);
            if (success) {
                Map<String, String> msg = new HashMap<>();
                msg.put("message", "Thêm sinh viên thành công!");
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, msg);
            } else {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Không thể lưu sinh viên vào cơ sở dữ liệu.");
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("error", "Lỗi xử lý yêu cầu: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }

    // 3. PUT: Update an existing student
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!checkSession(request, response)) return;
        try {
            Student student = readJsonBody(request, Student.class);
            
            if (student == null || student.getId() <= 0 || student.getStudentId() == null) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Dữ liệu cập nhật không hợp lệ.");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            // Check duplicate MSSV + subject excluding current student
            if (studentDAO.isStudentSubjectExists(student.getStudentId(), student.getSubject(), student.getId())) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Mã số sinh viên (MSSV) và Môn học này đã được đăng ký bởi bản ghi khác!");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            // Check if studentId is used by another student (different name/class) excluding current student
            if (studentDAO.isStudentIdMismatch(student.getStudentId(), student.getFullName(), student.getClassName(), student.getId())) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Mã số sinh viên (MSSV) này đã được đăng ký cho sinh viên khác!");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            // Check if this student (name/class) is registered under another studentId excluding current student
            if (studentDAO.isStudentIdentityMismatch(student.getStudentId(), student.getFullName(), student.getClassName(), student.getId())) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Sinh viên này đã có mã số sinh viên (MSSV) khác trong hệ thống!");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            boolean success = studentDAO.updateStudent(student);
            if (success) {
                Map<String, String> msg = new HashMap<>();
                msg.put("message", "Cập nhật sinh viên thành công!");
                sendJsonResponse(response, HttpServletResponse.SC_OK, msg);
            } else {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Không thể cập nhật thông tin sinh viên.");
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("error", "Lỗi xử lý yêu cầu: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }

    // 4. DELETE: Remove a student by database ID
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!checkSession(request, response)) return;
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Thiếu ID sinh viên cần xóa.");
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            boolean success = studentDAO.deleteStudent(id);
            if (success) {
                Map<String, String> msg = new HashMap<>();
                msg.put("message", "Xóa sinh viên thành công!");
                sendJsonResponse(response, HttpServletResponse.SC_OK, msg);
            } else {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Không tìm thấy sinh viên hoặc không thể xóa.");
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, err);
            }
        } catch (NumberFormatException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "ID sinh viên phải là một số nguyên.");
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("error", "Lỗi xử lý yêu cầu: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }
}
