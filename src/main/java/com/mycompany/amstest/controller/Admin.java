package com.mycompany.amstest.controller;

import com.google.gson.Gson;
import com.mycompany.amstest.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/admin/data")
public class Admin extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/assetsys";
    private static final String DB_USER = "assysAdmin";
    private static final String DB_PASS = "admin";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        Map<String, Object> data = new HashMap<>();

        if (session != null) {
            Map<String, String> sessionInfo = new HashMap<>();
            sessionInfo.put("username", (String) session.getAttribute("username"));
            sessionInfo.put("role", (String) session.getAttribute("role"));
            data.put("session", sessionInfo);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // 1. All assets
            List<Asset> assets = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT asset_id, asset_tag, asset_name, category, asset_status FROM assets")) {
                while (rs.next()) {
                    Asset a = new Asset();
                    a.setAssetId(rs.getInt("asset_id"));
                    a.setAssetTag(rs.getString("asset_tag"));
                    a.setAssetName(rs.getString("asset_name"));
                    a.setCategory(rs.getString("category"));
                    a.setAssetStatus(rs.getString("asset_status"));
                    assets.add(a);
                }
            }
            data.put("assets", assets);

            // 2. All users
            List<User> allUsers = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT userid, full_name, username, email, role, status FROM users")) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("userid"));
                    u.setFullName(rs.getString("full_name"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setRole(rs.getString("role"));
                    u.setStatus(rs.getString("status"));
                    allUsers.add(u);
                }
            }
            data.put("allUsers", allUsers);

            // 3. Active users (for assignment dropdown)
            List<User> activeUsers = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT userid, full_name, username FROM users WHERE status = 'ACTIVE'")) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("userid"));
                    u.setFullName(rs.getString("full_name"));
                    u.setUsername(rs.getString("username"));
                    activeUsers.add(u);
                }
            }
            data.put("users", activeUsers);

            // 4. Available assets (for assignment dropdown)
            List<Asset> availableAssets = new ArrayList<>();
            String availSql = "SELECT a.asset_id, a.asset_tag, a.asset_name FROM assets a " +
                "LEFT JOIN asset_assignments ass ON a.asset_id = ass.asset_id AND ass.returned_at IS NULL " +
                "WHERE ass.assignment_id IS NULL AND a.asset_status = 'AVAILABLE'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(availSql)) {
                while (rs.next()) {
                    Asset a = new Asset();
                    a.setAssetId(rs.getInt("asset_id"));
                    a.setAssetTag(rs.getString("asset_tag"));
                    a.setAssetName(rs.getString("asset_name"));
                    availableAssets.add(a);
                }
            }
            data.put("availableAssets", availableAssets);

            // 5. All assignments (including returned) – for Assigned Assets tab
            List<Assignment> assignments = new ArrayList<>();
            String assignSql = "SELECT ass.assignment_id, a.asset_tag, a.asset_name, u.full_name, ass.assigned_at, ass.returned_at, ass.notes " +
                "FROM asset_assignments ass " +
                "JOIN assets a ON ass.asset_id = a.asset_id " +
                "JOIN users u ON ass.user_id = u.userid";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(assignSql)) {
                while (rs.next()) {
                    Assignment as = new Assignment();
                    as.setAssignmentId(rs.getInt("assignment_id"));
                    as.setAssetTag(rs.getString("asset_tag"));
                    as.setAssetName(rs.getString("asset_name"));
                    as.setUserName(rs.getString("full_name"));
                    as.setAssignedAt(rs.getTimestamp("assigned_at").toString());
                    Timestamp returned = rs.getTimestamp("returned_at");
                    as.setReturnedAt(returned != null ? returned.toString() : null);
                    as.setNotes(rs.getString("notes")); // ✅ Notes added
                    assignments.add(as);
                }
            }
            data.put("assignments", assignments);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            return;
        }

        Gson gson = new Gson();
        response.getWriter().print(gson.toJson(data));
    }
}