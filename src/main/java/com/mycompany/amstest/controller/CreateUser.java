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

@WebServlet("/createUser")
public class CreateUser extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/assetsys";
    private static final String DB_USER = "assysAdmin";
    private static final String DB_PASS = "admin";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

        String fullName = request.getParameter("fullName");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String password = request.getParameter("password");
        String status = request.getParameter("status");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "INSERT INTO users (full_name, username, email, password, role, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, fullName);
                pstmt.setString(2, username);
                pstmt.setString(3, email);
                pstmt.setString(4, password);
                pstmt.setString(5, role);
                pstmt.setString(6, status);
                pstmt.executeUpdate();
            }
            result.put("success", true);
            result.put("message", "User created successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
        }

        response.getWriter().print(new Gson().toJson(result));
    }
}