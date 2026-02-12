package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly;
import com.usachevsergey.AssetBundleServer.database.enumerations.TokenType;
import com.usachevsergey.AssetBundleServer.database.repositories.UserRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.VerificationTokenRepository;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.database.tables.VerificationToken;
import com.usachevsergey.AssetBundleServer.exceptions.FieldNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class PageController {
    private final String templateHtml = "fragments/common";
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index(Model model) {
        generatePage("Главная", "index", model);
        return templateHtml;
    }
    @PreAuthorize("hasAuthority('DEVELOPER')")
    @EmailVerifiedOnly
    @GetMapping("/secured/uploadFile")
    public String files(Model model) {
        generatePage("Загрузка файла", "uploadFile", model);
        return templateHtml;
    }

    @GetMapping("/authorization")
    public String authorization(Model model) {
        generatePage("Авторизация", "authorization", model);
        return templateHtml;
    }

    @GetMapping("/forgotPassword")
    public String forgotPassword(Model model) {
        generatePage("Сброс пароля", "forgotPassword", model);
        return templateHtml;
    }

    @GetMapping("/resetPassword")
    public String resetPassword(@RequestParam String token, Model model) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, TokenType.PASSWORD_RESET).orElseThrow(() ->
                new FieldNotFoundException(HttpStatus.NOT_FOUND, "Токен не найден!"));

        if (verificationToken.isExpired()) {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Токен истек, запросите новый!");
            return "messagePage";
        }

        model.addAttribute("token", token);
        return "resetPassword";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        generatePage("Регистрация", "registration", model);
        return templateHtml;
    }

    @GetMapping("/secured/profile")
    public String profile(Model model) {
        generatePage("Профиль", "profile", model);
        return templateHtml;
    }

    @GetMapping("/store")
    public String store(Model model) {
        generatePage("Магазин", "store", model);
        return templateHtml;
    }

    @GetMapping("/bundle/{id}")
    public String bundle(Model model) {
        generatePage("Загрузка...", "bundle", model);
        return templateHtml;
    }

    @PreAuthorize("hasAuthority('USER')")
    @EmailVerifiedOnly
    @GetMapping("/secured/cart")
    public String cart(Model model) {
        generatePage("Корзина", "cart", model);
        return templateHtml;
    }

    @EmailVerifiedOnly
    @GetMapping("/secured/myBundles")
    public String myBundles(Model model) {
        generatePage("Мои бандлы", "myBundles", model);
        return templateHtml;
    }

    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam String token, Model model) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, TokenType.EMAIL_VERIFICATION).orElseThrow(() ->
                new FieldNotFoundException(HttpStatus.NOT_FOUND, "Токен не найден!"));

        if (verificationToken.isExpired()) {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Токен истек, запросите новый!");
            return "messagePage";
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        model.addAttribute("status", "success");
        model.addAttribute("message", "Email подтвержден!");

        return "messagePage";
    }

    @GetMapping("/errorPage")
    public String errorPage(@RequestParam String message, Model model) {
        model.addAttribute("status", "error");
        model.addAttribute("message", message);

        return "messagePage";
    }

    private void generatePage(String title, String pageHtmlName, Model model) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("fragmentPage", pageHtmlName);
    }
}
