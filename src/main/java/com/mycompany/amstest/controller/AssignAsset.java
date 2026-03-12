package com.mycompany.amstest.controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/assignAsset")
public class AssignAsset extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/assetsys";
    private static final String DB_USER = "assysAdmin";
    private static final String DB_PASS = "admin";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> result = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            result.put("success", false);
            result.put("message", "Unauthorized");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }

        int assetId;
        int userId;
        try {
            assetId = Integer.parseInt(request.getParameter("assetId"));
            userId = Integer.parseInt(request.getParameter("userId"));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Invalid asset or user ID");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }

        String assignedDate = request.getParameter("assignedDate"); // format dd/mm/yyyy

        // Convert dd/mm/yyyy to yyyy-mm-dd
        String[] parts = assignedDate.split("/");
        if (parts.length != 3) {
            result.put("success", false);
            result.put("message", "Invalid date format");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }
        String dbDate = parts[2] + "-" + parts[1] + "-" + parts[0]; // yyyy-mm-dd

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            // Insert assignment
            String insertSql = "INSERT INTO asset_assignments (asset_id, user_id, assigned_at) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, assetId);
                pstmt.setInt(2, userId);
                pstmt.setString(3, dbDate + " 00:00:00");
                pstmt.executeUpdate();
            }

            // Update asset status to ASSIGNED
            String updateSql = "UPDATE assets SET asset_status = 'ASSIGNED' WHERE asset_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, assetId);
                pstmt.executeUpdate();
            }

            result.put("success", true);
            result.put("message", "Asset assigned successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
        }

        response.getWriter().print(new Gson().toJson(result));
    }
}