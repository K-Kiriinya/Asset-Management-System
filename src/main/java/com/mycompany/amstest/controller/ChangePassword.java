package com.mycompany.amstest.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/changePassword")
public class ChangePassword extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/assetsys";
    private static final String DB_USER = "assysAdmin";
    private static final String DB_PASS = "admin";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> result = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "Not logged in");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            result.put("success", false);
            result.put("message", "New passwords do not match");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            // Verify current password
            String checkSql = "SELECT password FROM users WHERE userid = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    result.put("success", false);
                    result.put("message", "User not found");
                    response.getWriter().print(new Gson().toJson(result));
                    return;
                }
                String dbPassword = rs.getString("password");
                if (!dbPassword.equals(currentPassword)) {
                    result.put("success", false);
                    result.put("message", "Current password is incorrect");
                    response.getWriter().print(new Gson().toJson(result));
                    return;
                }
            }

            // Update password
            String updateSql = "UPDATE users SET password = ? WHERE userid = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newPassword);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }

            result.put("success", true);
            result.put("message", "Password changed successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
        }

        response.getWriter().print(new Gson().toJson(result));
    }
}