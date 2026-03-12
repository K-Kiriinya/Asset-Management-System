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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/user/data")
public class UserData extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/assetsys";
    private static final String DB_USER = "assysAdmin";
    private static final String DB_PASS = "admin";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);

        // Check if user is logged in
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(401);
            response.getWriter().print("{\"error\":\"Unauthorized\"}");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        List<UserAsset> assets = new ArrayList<>();

        String sql = "SELECT a.asset_tag, a.asset_name, a.category, ass.assigned_at, ass.notes " +
             "FROM asset_assignments ass " +
             "JOIN assets a ON ass.asset_id = a.asset_id " +
             "WHERE ass.user_id = ? AND ass.returned_at IS NULL";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UserAsset ua = new UserAsset();
                ua.setAssetTag(rs.getString("asset_tag"));
                ua.setAssetName(rs.getString("asset_name"));
                ua.setCategory(rs.getString("category"));
                ua.setAssignedAt(rs.getTimestamp("assigned_at").toString());
                ua.setStatus("ASSIGNED");
                ua.setNotes(rs.getString("notes"));
                assets.add(ua);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(500);
                return;
            }

        Gson gson = new Gson();
        response.getWriter().print(gson.toJson(assets));
    }

    // Simple inner class for JSON serialization
    private static class UserAsset {
        private String assetTag;
        private String assetName;
        private String category;
        private String assignedAt;
        private String status;
        private String notes;

        public String getAssetTag() { 
            return assetTag; 
        }
        
        public void setAssetTag(String assetTag) { 
            this.assetTag = assetTag; 
        }

        public String getAssetName() { 
            return assetName; 
        }
        
        public void setAssetName(String assetName) { 
            this.assetName = assetName; 
        }

        public String getCategory() { 
            return category; 
        }
        
        public void setCategory(String category) { 
            this.category = category; 
        }

        public String getAssignedAt() { 
            return assignedAt; 
        }
        
        public void setAssignedAt(String assignedAt) { 
            this.assignedAt = assignedAt; 
        }

        public String getStatus() { 
            return status; 
        }
        
        public void setStatus(String status) { 
            this.status = status; 
        }
        
        public String getNotes() { 
            return notes; 
        }
        
        public void setNotes(String notes) { 
            this.notes = notes; 
        }
    }
}