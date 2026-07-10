package com.stumanager.dao;

import com.stumanager.model.Student;
import com.stumanager.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // Get all students
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student s = new Student(
                    rs.getInt("id"),
                    rs.getString("student_id"),
                    rs.getString("full_name"),
                    rs.getString("class_name"),
                    rs.getString("subject"),
                    rs.getObject("tuition_fee") != null ? rs.getDouble("tuition_fee") : null
                );
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Add a new student
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students(student_id, full_name, class_name, subject, tuition_fee) VALUES(?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getFullName());
            pstmt.setString(3, student.getClassName());
            pstmt.setString(4, student.getSubject());
            if (student.getTuitionFee() != null) {
                pstmt.setDouble(5, student.getTuitionFee());
            } else {
                pstmt.setNull(5, java.sql.Types.DOUBLE);
            }
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update student details
    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET student_id = ?, full_name = ?, class_name = ?, subject = ?, tuition_fee = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getFullName());
            pstmt.setString(3, student.getClassName());
            pstmt.setString(4, student.getSubject());
            if (student.getTuitionFee() != null) {
                pstmt.setDouble(5, student.getTuitionFee());
            } else {
                pstmt.setNull(5, java.sql.Types.DOUBLE);
            }
            pstmt.setInt(6, student.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a student by database ID
    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if studentId (MSSV) already exists
    public boolean isStudentIdExists(String studentId, Integer excludeId) {
        String sql;
        if (excludeId != null) {
            sql = "SELECT COUNT(*) FROM students WHERE student_id = ? AND id != ?";
        } else {
            sql = "SELECT COUNT(*) FROM students WHERE student_id = ?";
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check if duplicate studentId and subject combination exists
    public boolean isStudentSubjectExists(String studentId, String subject, Integer excludeId) {
        String sql;
        if (excludeId != null) {
            sql = "SELECT COUNT(*) FROM students WHERE student_id = ? AND subject = ? AND id != ?";
        } else {
            sql = "SELECT COUNT(*) FROM students WHERE student_id = ? AND subject = ?";
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            pstmt.setString(2, subject);
            if (excludeId != null) {
                pstmt.setInt(3, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check if studentId belongs to a different student name/class
    public boolean isStudentIdMismatch(String studentId, String fullName, String className, Integer excludeId) {
        String sql;
        if (excludeId != null) {
            sql = "SELECT COUNT(*) FROM students WHERE student_id = ? AND (full_name != ? OR class_name != ?) AND id != ?";
        } else {
            sql = "SELECT COUNT(*) FROM students WHERE student_id = ? AND (full_name != ? OR class_name != ?)";
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId.trim());
            pstmt.setString(2, fullName.trim());
            pstmt.setString(3, className.trim());
            if (excludeId != null) {
                pstmt.setInt(4, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check if student identity (fullName, className) is associated with a different studentId
    public boolean isStudentIdentityMismatch(String studentId, String fullName, String className, Integer excludeId) {
        String sql;
        if (excludeId != null) {
            sql = "SELECT COUNT(*) FROM students WHERE full_name = ? AND class_name = ? AND student_id != ? AND id != ?";
        } else {
            sql = "SELECT COUNT(*) FROM students WHERE full_name = ? AND class_name = ? AND student_id != ?";
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fullName.trim());
            pstmt.setString(2, className.trim());
            pstmt.setString(3, studentId.trim());
            if (excludeId != null) {
                pstmt.setInt(4, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
