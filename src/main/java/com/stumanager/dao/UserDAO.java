package com.stumanager.dao;

import com.stumanager.util.DatabaseUtil;
import com.stumanager.util.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    
    /**
     * Verifies the username and password against database records.
     * Expects password to be plaintext, which is hashed inside this method for comparison.
     */
    public boolean checkLogin(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        
        String sql = "SELECT password FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String inputHash = HashUtil.hashPassword(password);
                    return storedHash.equals(inputHash);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while checking login credentials for user: " + username);
            e.printStackTrace();
        }
        return false;
    }
}
