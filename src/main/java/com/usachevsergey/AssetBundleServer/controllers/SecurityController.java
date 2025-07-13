package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.database.enumerations.TokenType;
import com.usachevsergey.AssetBundleServer.database.repositories.UserRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.VerificationTokenRepository;
import com.usachevsergey.AssetBundleServer.database.services.UserService;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.database.tables.VerificationToken;
import com.usachevsergey.AssetBundleServer.requests.SigninRequest;
import com.usachevsergey.AssetBundleServer.requests.SignupRequest;
import com.usachevsergey.AssetBundleServer.requests.UpdateUserRequest;
import com.usachevsergey.AssetBundleServer.security.authorization.JwtCookieManager;
import com.usachevsergey.AssetBundleServer.security.authorization.JwtCore;
import com.usachevsergey.AssetBundleServer.security.authorization.UserInputValidator;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/public/auth")
public class SecurityController {

    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtCore jwtCore;
    @Autowired
    JwtCookieManager jwtCookieManager;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository tokenRepository;

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
        User user = userService.createUser(signupRequest, passwordEncoder);
        userService.sendVerificationEmail(user);
        return ResponseEntity.ok(Map.of("message", "Регистрация прошла успешно!"));
    }

   @PostMapping("/signin")
    ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest, HttpServletResponse response) {
       Authentication authentication;
       try {
           authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getUsername(), signinRequest.getPassword()));
       } catch (BadCredentialsException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Неверные имя пользователя или пароль!"));
       }
       SecurityContextHolder.getContext().setAuthentication(authentication);
       String jwt = jwtCore.generateToken(authentication);

       jwtCookieManager.saveToken(jwt, response);

       return ResponseEntity.ok(Map.of("message","Авторизация прошла успешно!"));
   }

   @PostMapping("/logout")
    ResponseEntity<?> logout(HttpServletResponse response) {
        jwtCookieManager.clearToken(response);

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("message","Выход выполнен успешно!"));
   }

   @GetMapping("/verifyEmail")
    ResponseEntity<?> verifyEmail(@RequestParam String token) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, TokenType.EMAIL_VERIFICATION).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Токен не найден!"));

        if (verificationToken.isExpired()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Токен истек, запросите новый!"));
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        return ResponseEntity.ok(Map.of("message", "Email подтвержден!"));
   }

    @PostMapping("/resetPassword/request")
    ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с таким email не найден!"));

        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Подтвердите адрес электронной почты!");
        }

        String message = UserInputValidator.validateEmail(email);
        if (message != null) {
            throw new IllegalArgumentException(message);
        }
        userService.sendResetPasswordEmail(user);

        return ResponseEntity.ok(Map.of("message", "Сообщение о сбросе пароля отправлено на почту!"));
    }

    @PutMapping("/resetPassword/confirm")
    ResponseEntity<?> confirmPasswordReset(@RequestBody UpdateUserRequest request) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(request.getVerToken(), TokenType.PASSWORD_RESET).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Токен не найден!"));

        if (!verificationToken.getUser().isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Подтвердите адрес электронной почты!");
        }

        if (verificationToken.isExpired()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Токен истек, запросите новый!"));
        }

        userService.resetUserPassword(verificationToken.getUser(), request, passwordEncoder);
        tokenRepository.delete(verificationToken);

        return ResponseEntity.ok(Map.of("message", "Пароль обновлен!"));
    }
}
