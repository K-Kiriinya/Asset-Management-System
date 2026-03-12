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

@WebServlet("/reactivateAsset")
public class ReactivateAsset extends HttpServlet {

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
        try {
            assetId = Integer.parseInt(request.getParameter("assetId"));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Invalid asset ID");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Update asset status to AVAILABLE
            String sql = "UPDATE assets SET asset_status = 'AVAILABLE' WHERE asset_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, assetId);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    result.put("success", true);
                    result.put("message", "Asset reactivated successfully");
                } else {
                    result.put("success", false);
                    result.put("message", "Asset not found");
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