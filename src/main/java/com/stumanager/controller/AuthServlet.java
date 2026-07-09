package com.stumanager.controller;

import com.google.gson.Gson;
import com.stumanager.dao.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth")
public class AuthServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        try (PrintWriter out = response.getWriter()) {
            out.print(this.gson.toJson(data));
            out.flush();
        }
    }

    private <T> T readJsonBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        request.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), clazz);
    }

    // GET /api/auth -> check login status
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();
        if (session != null && session.getAttribute("user") != null) {
            result.put("loggedIn", true);
            result.put("username", session.getAttribute("user"));
        } else {
            result.put("loggedIn", false);
        }
        sendJsonResponse(response, HttpServletResponse.SC_OK, result);
    }

    // POST /api/auth -> login
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            LoginRequest loginReq = readJsonBody(request, LoginRequest.class);
            if (loginReq == null || loginReq.username == null || loginReq.password == null) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Tên đăng nhập hoặc mật khẩu không hợp lệ.");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            boolean success = userDAO.checkLogin(loginReq.username, loginReq.password);
            if (success) {
                HttpSession session = request.getSession(true);
                session.setMaxInactiveInterval(600); // 10 minutes session timeout
                session.setAttribute("user", loginReq.username);

                Map<String, Object> msg = new HashMap<>();
                msg.put("message", "Đăng nhập thành công!");
                msg.put("username", loginReq.username);
                sendJsonResponse(response, HttpServletResponse.SC_OK, msg);
            } else {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, err);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("error", "Lỗi xử lý yêu cầu: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }

    // DELETE /api/auth -> logout
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Map<String, String> msg = new HashMap<>();
        msg.put("message", "Đã đăng xuất thành công.");
        sendJsonResponse(response, HttpServletResponse.SC_OK, msg);
    }

    // Static helper class for request parsing
    private static class LoginRequest {
        String username;
        String password;
    }
}
