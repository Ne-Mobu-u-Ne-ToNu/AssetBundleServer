package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.UpdateUserRequest;
import com.usachevsergey.AssetBundleServer.User;
import com.usachevsergey.AssetBundleServer.UserDetailsImpl;
import com.usachevsergey.AssetBundleServer.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/secured")
@RequiredArgsConstructor
public class MainUserController {
    private final String unauthorizedMessage = "Пользователь не авторизован";
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/user")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "email", userDetails.getEmail(),
                "api_key", userDetails.getApiKey(),
                "role", userDetails.getRole(),
                "email_verified", userDetails.isEmailVerified(),
                "created_at", userDetails.getCreatedAt()
        ));
    }

    @GetMapping("/apiKey")
    public ResponseEntity<?> apiKey(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }
        return ResponseEntity.ok(Map.of("api_key", userDetails.getApiKey()));
    }

    @PutMapping("/updateUser")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody UpdateUserRequest request,
                                        HttpServletResponse response) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }

        userService.updateUser(userDetails.getUsername(), request, response, passwordEncoder);
        return ResponseEntity.ok(Map.of("message", "Данные пользователя обновлены!"));
    }

    @PostMapping("/sendVerificationEmail")
    public ResponseEntity<?> sendVerificationEmail(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }
        userService.sendVerificationEmail(userDetails.getUsername());

        return ResponseEntity.ok(Map.of("message", "Сообщение отправлено на почту!"));
    }

    @PutMapping("/generateApiKey")
    public ResponseEntity<?> generateApiKey(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }
        userService.generateApiKey(userDetails.getUsername());

        return ResponseEntity.ok(Map.of("message", "Api-ключ сгенерирован!"));
    }
}
