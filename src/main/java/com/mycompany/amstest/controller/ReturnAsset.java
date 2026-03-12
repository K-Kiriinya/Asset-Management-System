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

@WebServlet("/returnAsset")
public class ReturnAsset extends HttpServlet {

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

        int assignmentId;
        try {
            assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Invalid assignment ID");
            response.getWriter().print(new Gson().toJson(result));
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            try {
                
                // Get asset_id
                String getAssetSql = "SELECT asset_id FROM asset_assignments WHERE assignment_id = ?";
                int assetId;
                try (PreparedStatement pstmt = conn.prepareStatement(getAssetSql)) {
                    pstmt.setInt(1, assignmentId);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("Assignment not found");
                    }
                    assetId = rs.getInt("asset_id");
                }

                // Set returned_at to current timestamp
                String updateSql = "UPDATE asset_assignments SET returned_at = CURRENT_TIMESTAMP WHERE assignment_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, assignmentId);
                    pstmt.executeUpdate();
                }

                // Update asset status to AVAILABLE
                String updateAssetSql = "UPDATE assets SET asset_status = 'AVAILABLE' WHERE asset_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateAssetSql)) {
                    pstmt.setInt(1, assetId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                result.put("success", true);
                result.put("message", "Asset returned successfully");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
        }

        response.getWriter().print(new Gson().toJson(result));
    }
}