package com.mycompany.amstest.controller;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class Login extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/assetsys";
    private static final String DB_USER = "assysAdmin";
    private static final String DB_PASS = "admin";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("login.xhtml");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("=== LOGIN POST HIT ===");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        System.out.println("Username: " + username);

        // Validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            System.out.println("Validation failed: missing fields");
            response.sendRedirect("login.xhtml?error=missing");
            return;
        }

        // Authenticate and retrieve user details
        UserAuthResult authResult = authenticateUser(username, password);
        if (authResult == null) {
            System.out.println("Authentication failed");
            response.sendRedirect("login.xhtml?error=invalid");
            return;
        }

        // Login successful – store user info in session
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", authResult.userId);
        session.setAttribute("username", username);
        session.setAttribute("role", authResult.role);
        session.setAttribute("email", authResult.email);
        session.setAttribute("fullName", authResult.fullName); // <-- NEW: store full name

        // Redirect to appropriate dashboard
        String ctx = request.getContextPath();
        if ("ADMIN".equalsIgnoreCase(authResult.role)) {
            response.sendRedirect(ctx + "/admin-dashboard.xhtml");
        } else {
            response.sendRedirect(ctx + "/user-dashboard.xhtml");
        }
    }

    /**
     * Authenticates a user against the database.
     * @return UserAuthResult containing userId, role, email, and fullName, or null if authentication fails.
     */
    private UserAuthResult authenticateUser(String username, String password) {
        String sql = "SELECT userid, password, role, email, full_name FROM users WHERE username = ?";

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            System.out.println("MariaDB JDBC Driver loaded successfully");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                System.out.println("Executing query for username: " + username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No user found with username: " + username);
                        return null;
                    }

                    int userId = rs.getInt("userid");
                    String dbPassword = rs.getString("password");
                    String role = rs.getString("role");
                    String email = rs.getString("email");
                    String fullName = rs.getString("full_name"); // <-- NEW: get full name
                    System.out.println("DB password: " + dbPassword + ", role: " + role + ", email: " + email + ", fullName: " + fullName);

                    if (dbPassword != null && dbPassword.equals(password)) {
                        System.out.println("Password match – authentication successful");
                        // Return all fields
                        return new UserAuthResult(userId, role, email, fullName);
                    } else {
                        System.out.println("Password mismatch");
                        return null;
                    }
                }
            } catch (SQLException e) {
                System.out.println("SQL Exception in authenticateUser:");
                e.printStackTrace();
                return null;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Class to hold authentication result.
     */
    private static class UserAuthResult {
        int userId;
        String role;
        String email;
        String fullName;

        UserAuthResult(int userId, String role, String email, String fullName) {
            this.userId = userId;
            this.role = role;
            this.email = email;
            this.fullName = fullName;
        }
    }
}