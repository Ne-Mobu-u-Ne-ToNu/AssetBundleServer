package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/secured")
public class MainUserController {
    private final String unauthorizedMessage = "Пользователь не авторизован";
    @GetMapping("/myName")
    public ResponseEntity<?> userAccess(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorizedMessage);
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorizedMessage);
        }
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "email", userDetails.getEmail()
        ));
    }
}
