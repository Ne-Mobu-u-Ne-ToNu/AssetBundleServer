package com.usachevsergey.AssetBundleServer;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtCore jwtCore;
    @Autowired
    JwtCookieManager jwtCookieManager;
    @Value("${server.app.verTokenLifeHours}")
    private int verifyTokenLifetime;
    @Value("${server.app.verifyEmailLink}")
    private String verifyEmailLink;
    @Value("${server.app.resetPasswordLink}")
    private String resetPasswordLink;

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUser(username);

        return UserDetailsImpl.build(user);
    }

    public User createUser(SignupRequest signupRequest, PasswordEncoder passwordEncoder) throws IllegalArgumentException {
        String validation = UserInputValidator.validateUser(signupRequest);
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
        validation = UserInputValidator.validateEmail(signupRequest.getEmail());
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
        validation = UserInputValidator.validatePasswordsMatch(signupRequest.getPassword(), signupRequest.getConfPassword());
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
        validation = UserInputValidator.validatePassword(signupRequest.getPassword());
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
        if (userRepository.existsUserByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("Выберите другое имя пользователя");
        }
        if (userRepository.existsUserByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Выберите другой адрес электронной почты");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setApiKey(generateApiKeyAndToken());
        user.setRole(signupRequest.getRole());
        userRepository.save(user);

        return user;
    }

    public void updateUser(String username, UpdateUserRequest request,
                             HttpServletResponse response, PasswordEncoder passwordEncoder) throws UsernameNotFoundException {
        User user = getUser(username);
        boolean changed = false;

        String newData = request.getNewUsername();
        if (!UserInputValidator.isNullOrEmpty(newData) && !user.getUsername().equals(newData)) {
            if (userRepository.existsUserByUsername(newData)) {
                throw new IllegalArgumentException("Выберите другое имя пользователя");
            }
            user.setUsername(newData);
            changed = true;
        }

        newData = request.getNewEmail();
        if (!UserInputValidator.isNullOrEmpty(newData) && !user.getEmail().equals(newData) &&
        UserInputValidator.validateEmail(newData) == null) {
            if (userRepository.existsUserByEmail(newData)) {
                throw new IllegalArgumentException("Выберите другой адрес электронной почты");
            }
            user.setEmail(newData);
            user.setEmailVerified(false);
            changed = true;
        }

        if (!changed) {
            throw new IllegalArgumentException("Данные пользователя не обновлены!");
        }
        userRepository.save(user);

        UserDetails userDetails = loadUserByUsername(user.getUsername());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtCore.generateToken(auth);

        jwtCookieManager.saveToken(jwt, response);
    }

    public boolean isValidApiKey(String apiKey) {
        User user = userRepository.findUserByApiKey(apiKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден!"));

        return user != null;
    }

    public void sendVerificationEmail(User user) {
        String link = verifyEmailLink + saveToken(TokenType.EMAIL_VERIFICATION, user);
        String message = "Для подтверждения email перейдите по ссылке " + link;

        emailService.sendVerificationEmail(user.getEmail(), message, "Подтверждение email");
    }

    public void sendVerificationEmail(String userName) {
        User user = getUser(userName);
        sendVerificationEmail(user);
    }

    public void sendResetPasswordEmail(User user) {
        String link = resetPasswordLink + saveToken(TokenType.PASSWORD_RESET, user);
        String message = "Для сброса пароля перейдите по ссылке " + link;

        emailService.sendVerificationEmail(user.getEmail(), message, "Сброс пароля");
    }

    public void resetUserPassword(User user, UpdateUserRequest request, PasswordEncoder passwordEncoder) {
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Пароль не должен быть прежним!");
        }
        String validation = UserInputValidator.validatePasswordsMatch(request.getNewPassword(), request.getConfPassword());
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
        validation = UserInputValidator.validatePassword(request.getNewPassword());
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void generateApiKey(String userName) {
        User user = getUser(userName);

        user.setApiKey(generateApiKeyAndToken());
        userRepository.save(user);
    }

    private String saveToken(TokenType type, User user) {
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user);
        if (verificationTokenRepository.findByUser(user) == null) {
            verificationToken = new VerificationToken();
        }

        String token = generateApiKeyAndToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setType(type);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(verifyTokenLifetime));

        verificationTokenRepository.save(verificationToken);

        return token;
    }

    private User getUser(String username) {
        return userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("Пользователь '%s' не найден", username)
        ));
    }

    private String generateApiKeyAndToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
