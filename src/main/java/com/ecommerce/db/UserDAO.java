package com.ecommerce.db;

import com.ecommerce.model.User;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {

    private Connection connection;

    public UserDAO() {
        connection = DatabaseManager.getInstance().getConnection();
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, address, gcash_number, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            // Hash the password before storing
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getAddress());
            pstmt.setString(5, user.getGcashNumber());
            pstmt.setString(6, "CUSTOMER");   // default role
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                System.err.println("Username already exists.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    public User registerAndGetUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, address, gcash_number, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            // Hash the password before storing
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getAddress());
            pstmt.setString(5, user.getGcashNumber());
            pstmt.setString(6, "CUSTOMER");
            int affected = pstmt.executeUpdate();
            if (affected == 0) return null;
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                user.setId(id);
                return user;
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User login(String username, String password) {
        // First get the user by username (to retrieve the hashed password)
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                // Check if the stored password is a bcrypt hash
                if (hashedPassword != null && (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$"))) {
                    // Verify with bcrypt
                    if (!BCrypt.checkpw(password, hashedPassword)) {
                        return null; // wrong password
                    }
                } else {
                    // Legacy plaintext password — hash now, but verify against plaintext
                    if (!password.equals(hashedPassword)) {
                        return null; // wrong password
                    }
                    // Upgrade to bcrypt hash
                    String newHash = BCrypt.hashpw(password, BCrypt.gensalt());
                    String updateSql = "UPDATE users SET password = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setString(1, newHash);
                        updateStmt.setInt(2, rs.getInt("id"));
                        updateStmt.executeUpdate();
                    }
                }
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("address"),
                    rs.getString("gcash_number"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}