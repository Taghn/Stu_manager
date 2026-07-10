package com.stumanager.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Stu_manager?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    static {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Auto-create database if not exists
            createDatabaseIfNotExists();
            
            // Initialize the database tables
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add MySQL dependency to pom.xml.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void createDatabaseIfNotExists() {
        try (Connection conn = DriverManager.getConnection(SERVER_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS Stu_manager;");
            System.out.println("Database 'Stu_manager' verified/created successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to create database 'Stu_manager'. Make sure MySQL is running and credentials are correct.");
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() {
        String createStudentsTableSQL = "CREATE TABLE IF NOT EXISTS students (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "student_id VARCHAR(50) NOT NULL, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "class_name VARCHAR(50) NOT NULL, " +
                "subject VARCHAR(100) NULL, " +
                "tuition_fee DOUBLE DEFAULT 0.0, " +
                "UNIQUE KEY unique_student_subject (student_id, subject)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create tables
            stmt.execute(createStudentsTableSQL);
            stmt.execute(createUsersTableSQL);

            // Dynamically alter students table to add new columns if they do not exist
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            
            // Check 'subject' column
            try (java.sql.ResultSet rs = metaData.getColumns(null, null, "students", "subject")) {
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE students ADD COLUMN subject VARCHAR(100) NULL;");
                    System.out.println("Column 'subject' added to 'students' table.");
                }
            }

            // Check 'tuition_fee' column
            try (java.sql.ResultSet rs = metaData.getColumns(null, null, "students", "tuition_fee")) {
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE students ADD COLUMN tuition_fee DOUBLE DEFAULT 0.0;");
                    System.out.println("Column 'tuition_fee' added to 'students' table.");
                }
            }

            // Drop 'email' column if it exists in the 'students' table to prevent INSERT failures
            try (java.sql.ResultSet rs = metaData.getColumns(null, null, "students", "email")) {
                if (rs.next()) {
                    stmt.execute("ALTER TABLE students DROP COLUMN email;");
                    System.out.println("Column 'email' dropped from 'students' table successfully.");
                }
            }

            // Update UNIQUE constraint: drop student_id single unique index and add composite unique index
            boolean hasStudentIdUnique = false;
            boolean hasCompositeUnique = false;
            try (java.sql.ResultSet rs = metaData.getIndexInfo(null, null, "students", false, false)) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    if ("student_id".equalsIgnoreCase(indexName)) {
                        hasStudentIdUnique = true;
                    }
                    if ("unique_student_subject".equalsIgnoreCase(indexName)) {
                        hasCompositeUnique = true;
                    }
                }
            }

            if (hasStudentIdUnique) {
                try {
                    stmt.execute("ALTER TABLE students DROP INDEX student_id;");
                    System.out.println("Dropped single UNIQUE index on student_id.");
                } catch (SQLException e) {
                    System.err.println("Warning: failed to drop index student_id: " + e.getMessage());
                }
            }

            if (!hasCompositeUnique) {
                try {
                    stmt.execute("ALTER TABLE students ADD UNIQUE KEY unique_student_subject (student_id, subject);");
                    System.out.println("Added composite UNIQUE key (student_id, subject) to students table.");
                } catch (SQLException e) {
                    System.err.println("Warning: failed to add composite unique key: " + e.getMessage());
                }
            }
            
            // Seed default admin user if users table is empty
            String checkUsersSQL = "SELECT COUNT(*) FROM users;";
            try (java.sql.ResultSet rs = stmt.executeQuery(checkUsersSQL)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String adminHash = HashUtil.hashPassword("123123");
                    String seedUserSQL = "INSERT INTO users (username, password) VALUES ('admin', '" + adminHash + "');";
                    stmt.execute(seedUserSQL);
                    System.out.println("Default admin user seeded successfully in MySQL.");
                }
            }

            // Seed default student data if students table is empty
            String checkStudentsSQL = "SELECT COUNT(*) FROM students;";
            try (java.sql.ResultSet rs = stmt.executeQuery(checkStudentsSQL)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String seedStudentsSQL = "INSERT INTO students (student_id, full_name, class_name, subject, tuition_fee) VALUES " +
                            "('2305CT0001', 'Nguyễn Văn A', 'CT07PM', 'Lập trình Java', 3500000.0), " +
                            "('2305CT0001', 'Nguyễn Văn A', 'CT07PM', 'Cơ sở dữ liệu', 4000000.0), " +
                            "('2305CT0002', 'Trần Thị B', 'CT07PM', 'Cơ sở dữ liệu', 4000000.0), " +
                            "('2305CT0003', 'Lê Hoàng C', 'CT08PM', 'Phân tích thiết kế hệ thống', 3800000.0), " +
                            "('2305CT0004', 'Phạm Minh D', 'CT07PM', 'Lập trình Web nâng cao', 4500000.0), " +
                            "('2305CT0005', 'Vũ Hoài E', 'CT08PM', 'An toàn thông tin', 3600000.0);";
                    stmt.execute(seedStudentsSQL);
                    System.out.println("Default students seeded successfully in MySQL.");
                }
            }
            
            System.out.println("Database initialized successfully at: " + DB_URL);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database tables in MySQL.");
            e.printStackTrace();
        }
    }
}
