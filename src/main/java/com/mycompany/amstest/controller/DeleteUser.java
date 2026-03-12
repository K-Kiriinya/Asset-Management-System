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

@WebServlet("/deleteUser")
public class DeleteUser extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/assetsys";
    private static final String DB_USER = "assysAdmin";
    private static final String DB_PASS = "admin";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> result = new HashMap<>();

        // Check admin session
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            result.put("success", false);
            result.put("message", "Unauthorized");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(request.getParameter("userId"));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Invalid user ID");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Check for any assiged assets
            String checkSql = "SELECT COUNT(*) FROM asset_assignments WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                if (count > 0) {
                    result.put("success", false);
                    result.put("message", "Cannot delete user who has been assigned assets.");
                    response.getWriter().print(new Gson().toJson(result));
                    return;
                }
            }

            // Delete user
            String deleteSql = "DELETE FROM users WHERE userid = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, userId);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    result.put("success", true);
                    result.put("message", "User deleted successfully");
                } else {
                    result.put("success", false);
                    result.put("message", "User not found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
        }

        response.getWriter().print(new Gson().toJson(result));
    }
}