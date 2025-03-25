package com.usachevsergey.AssetBundleServer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String index() {
        return "index";
    }
    @GetMapping("/uploadFile")
    public String files() {
        return "uploadFile";
    }

    @GetMapping("/authorization")
    public String authorization() {
        return "authorization";
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }
}
