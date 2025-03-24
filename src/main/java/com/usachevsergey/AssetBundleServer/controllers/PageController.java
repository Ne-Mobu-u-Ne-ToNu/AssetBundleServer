package com.usachevsergey.AssetBundleServer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/files")
    public String index() {
        return "files";
    }


}
