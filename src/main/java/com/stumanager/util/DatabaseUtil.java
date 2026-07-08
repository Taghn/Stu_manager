package com.stumanager.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String DB_NAME = "students.db";
    private static String dbUrl = "";

    static {
        try {
            // Load SQLite JDBC Driver
            Class.forName("org.sqlite.JDBC");
            
            // Save database file in user's home directory to make it persistent and accessible
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + File.separator + DB_NAME;
            dbUrl = "jdbc:sqlite:" + dbPath;

            // Initialize the database table
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found. Add SQLite dependency to pom.xml.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dbUrl.isEmpty()) {
            throw new SQLException("Database URL is not initialized.");
        }
        return DriverManager.getConnection(dbUrl);
    }

    private static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS students (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "student_id TEXT NOT NULL UNIQUE, " +
                "full_name TEXT NOT NULL, " +
                "class_name TEXT NOT NULL, " +
                "email TEXT NOT NULL" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Database initialized successfully at: " + dbUrl);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database table.");
            e.printStackTrace();
        }
    }
}
