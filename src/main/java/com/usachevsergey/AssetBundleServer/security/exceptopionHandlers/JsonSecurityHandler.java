package com.usachevsergey.AssetBundleServer.security.exceptopionHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String message = "Требуется авторизация для доступа к ресурсу!";
        redirect(request, response, message, HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String message;
        if (accessDeniedException instanceof AuthorizationDeniedException) {
            message = "У вас не та роль! Смените учетную запись";
        } else {
            message = "Ошибка доступа к ресурсу!";
        }

        redirect(request, response, message, HttpServletResponse.SC_FORBIDDEN);
    }

    private void writeJsonResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, String> body = Map.of("error", message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String message, int responseCode) throws IOException {
        if (isApiRequest(request)) {
            writeJsonResponse(response, responseCode, message);
        } else {
            response.sendRedirect("/errorPage?message=" + URLEncoder.encode(message, StandardCharsets.UTF_8)
                    + "&errorCode=" + responseCode);
        }
    }
}
