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
import java.util.Map;

public class JsonSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Требуется авторизация для доступа к ресурсу!");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String message;
        if (accessDeniedException instanceof AuthorizationDeniedException) {
            message = "У вас не та роль! Смените учетную запись";
        } else {
            message = "Ошибка доступа к ресурсу!";
        }

        writeJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, message);
    }

    private void writeJsonResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, String> body = Map.of("error", message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
