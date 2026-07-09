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
                "student_id VARCHAR(50) NOT NULL UNIQUE, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "class_name VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL" +
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
            
            System.out.println("Database initialized successfully at: " + DB_URL);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database tables in MySQL.");
            e.printStackTrace();
        }
    }
}
