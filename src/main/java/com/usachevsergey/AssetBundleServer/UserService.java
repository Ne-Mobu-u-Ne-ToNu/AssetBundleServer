package com.usachevsergey.AssetBundleServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUser(username);

        return UserDetailsImpl.build(user);
    }

    public void createUser(SignupRequest signupRequest, PasswordEncoder passwordEncoder) throws IllegalArgumentException {
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
        user.setApiKey(generateApiKey());
        userRepository.save(user);
    }

    public void updateUser(String username, UpdateUserRequest request, PasswordEncoder passwordEncoder) throws UsernameNotFoundException {
        User user = getUser(username);

        userRepository.save(user);
    }

    public boolean isValidApiKey(String apiKey) {
        User user = userRepository.findUserByApiKey(apiKey);

        return user != null;
    }

    private User getUser(String username) {
        return userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("Пользователь '%s' не найден", username)
        ));
    }

    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
