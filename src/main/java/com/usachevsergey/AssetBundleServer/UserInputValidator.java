package com.usachevsergey.AssetBundleServer;

import java.util.Objects;

public class UserInputValidator {

    public static String validateUser(SignupRequest signupRequest) {
        if (isNullOrEmpty(signupRequest.getUsername())) {
            return "Имя пользователя обязательно для заполнения";
        }
        if (isNullOrEmpty(signupRequest.getEmail())) {
            return "Email обязателен для заполнения";
        }
        if (isNullOrEmpty(signupRequest.getPassword())) {
            return "Пароль обязателен для заполнения";
        }
        if (isNullOrEmpty(signupRequest.getConfPassword())) {
            return "Подтверждение пароля обязательно";
        }
        if (signupRequest.getRole() == null) {
            return "Поле роль обязательно для заполнения";
        }
        return null;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            return "Неверный формат email!";
        }
        return null;
    }

    public static String validatePasswordsMatch(String password, String confPassword) {
        if (!Objects.equals(password, confPassword)) {
            return "Пароли не совпадают";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (password.length() < 8) {
            return "Длина пароля минимум 8 символов";
        }
        if (!password.matches(".*[A-Z]*")) {
            return "Минимум одна прописная латинская буква";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Минимум одна строчная латинская буква";
        }
        if (!password.matches(".*\\d.*")) {
            return "Минимум одна цифра";
        }
        if (!password.matches(".*[!\"$%&'()+,\\-./:;<=>?@\\[\\]^_{|}~`].*")) {
            return "Пароль должен содержать хотя бы один специальный символ (!\"$%&'()+,-./:;<=>?@[]^_{|}~`).";
        }
        return null;
    }
}
