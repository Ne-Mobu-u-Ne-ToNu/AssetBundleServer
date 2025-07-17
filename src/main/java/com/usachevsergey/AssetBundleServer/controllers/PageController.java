package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {
    private final String templateHtml = "fragments/common";

    @GetMapping("/")
    public String index(Model model) {
        generatePage("Главная", "index", model);
        return templateHtml;
    }
    @PreAuthorize("hasAuthority('DEVELOPER')")
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

    private void generatePage(String title, String pageHtmlName, Model model) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("fragmentPage", pageHtmlName);
    }
}
