package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.UpdateUserRequest;
import com.usachevsergey.AssetBundleServer.UserDetailsImpl;
import com.usachevsergey.AssetBundleServer.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/secured")
@RequiredArgsConstructor
public class MainUserController {
    private final String unauthorizedMessage = "Пользователь не авторизован";
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/username")
    public ResponseEntity<?> userAccess(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }

    @GetMapping("/user")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "email", userDetails.getEmail(),
                "api_key", userDetails.getApiKey(),
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
                                        @RequestBody UpdateUserRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }

        try {
            userService.updateUser(userDetails.getUsername(), request, passwordEncoder);
            return ResponseEntity.ok("Данные пользователя обновлены!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка на сервере");
        }
    }
}
