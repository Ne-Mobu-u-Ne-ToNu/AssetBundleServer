package com.usachevsergey.AssetBundleServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    @Value("${server.app.verTokenLifeHours}")
    private int verifyTokenLifetime;
    @Value("${server.app.verifyEmailLink}")
    private String verifyEmailLink;

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
        userRepository.save(user);

        return user;
    }

    public void updateUser(String username, UpdateUserRequest request, PasswordEncoder passwordEncoder) throws UsernameNotFoundException {
        User user = getUser(username);

        userRepository.save(user);
    }

    public boolean isValidApiKey(String apiKey) {
        User user = userRepository.findUserByApiKey(apiKey);

        return user != null;
    }

    public void sendVerificationEmail(User user) {
        String token = generateApiKeyAndToken();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(verifyTokenLifetime));

        verificationTokenRepository.save(verificationToken);

        String link = verifyEmailLink + token;
        String message = "Для подтверждения email перейдите по ссылке " + link;

        emailService.sendVerificationEmail(user.getEmail(), message);
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
