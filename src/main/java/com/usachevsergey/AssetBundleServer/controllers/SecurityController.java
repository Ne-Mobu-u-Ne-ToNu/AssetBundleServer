package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@RestController
@RequestMapping("/auth")
public class SecurityController {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtCore jwtCore;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
        String validation = UserInputValidator.validateUser(signupRequest);
        if (validation != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validation);
        }
        validation = UserInputValidator.validateEmail(signupRequest.getEmail());
        if (validation != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validation);
        }
        validation = UserInputValidator.validatePasswordsMatch(signupRequest.getPassword(), signupRequest.getConfPassword());
        if (validation != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validation);
        }
        validation = UserInputValidator.validatePassword(signupRequest.getPassword());
        if (validation != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validation);
        }
        if (userRepository.existsUserByUsername(signupRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Выберите другое имя пользователя");
        }
        if (userRepository.existsUserByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Выберите другой адрес электронной почты");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Регистрация прошла успешно!");
    }

   @PostMapping("/signin")
    ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest, HttpServletResponse response) {
       Authentication authentication;
       try {
           authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getUsername(), signinRequest.getPassword()));
       } catch (BadCredentialsException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверные имя пользователя или пароль");
       }
       SecurityContextHolder.getContext().setAuthentication(authentication);
       String jwt = jwtCore.generateToken(authentication);

       Cookie cookie = new Cookie("jwt", jwt);
       cookie.setHttpOnly(true);
       cookie.setPath("/");
       cookie.setMaxAge(jwtCore.getLifetime());
       response.addCookie(cookie);
       return ResponseEntity.ok("Авторизация прошла успешно!");
   }

   @PostMapping("/logout")
    ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Выход выполнен успешно!");
   }
}
