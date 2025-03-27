package com.usachevsergey.AssetBundleServer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    private String templateHtml = "fragments/common";

    @GetMapping("/")
    public String index(Model model) {
        generatePage("Главная", "index", model);
        return templateHtml;
    }
    @GetMapping("/uploadFile")
    public String files(Model model) {
        generatePage("Загрузка файла", "uploadFile", model);
        return templateHtml;
    }

    @GetMapping("/authorization")
    public String authorization(Model model) {
        generatePage("Авторизация", "authorization", model);
        return templateHtml;
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        generatePage("Регистрация", "registration", model);
        return templateHtml;
    }

    private void generatePage(String title, String pageHtmlName, Model model) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("fragmentPage", pageHtmlName);
    }
}
