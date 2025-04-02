package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/auth")
public class SecurityController {

    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtCore jwtCore;
    @Autowired
    private UserService userService;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @PostMapping("/signup")
    ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            userService.createUser(signupRequest, passwordEncoder);
            return ResponseEntity.ok(Map.of("message", "Регистрация прошла успешно!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Произошла ошибка на сервере"));
        }
    }

   @PostMapping("/signin")
    ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest, HttpServletResponse response) {
       Authentication authentication;
       try {
           authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getUsername(), signinRequest.getPassword()));
       } catch (BadCredentialsException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Неверные имя пользователя или пароль"));
       }
       SecurityContextHolder.getContext().setAuthentication(authentication);
       String jwt = jwtCore.generateToken(authentication);

       Cookie cookie = new Cookie("jwt", jwt);
       cookie.setHttpOnly(true);
       cookie.setPath("/");
       cookie.setMaxAge(jwtCore.getLifetime());
       response.addCookie(cookie);
       return ResponseEntity.ok(Map.of("message","Авторизация прошла успешно!"));
   }

   @PostMapping("/logout")
    ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("message","Выход выполнен успешно!"));
   }
}
