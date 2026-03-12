package com.mycompany.amstest.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/user/info")
public class UserInfo extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        Map<String, String> result = new HashMap<>();
        
        if (session != null && session.getAttribute("username") != null) {
            result.put("username", (String) session.getAttribute("username"));
            result.put("fullName", (String) session.getAttribute("fullName"));
            result.put("role", (String) session.getAttribute("role"));
            result.put("email", (String) session.getAttribute("email"));
        } else {
            result.put("error", "Not logged in");
        }
        response.getWriter().print(new Gson().toJson(result));
    }
}