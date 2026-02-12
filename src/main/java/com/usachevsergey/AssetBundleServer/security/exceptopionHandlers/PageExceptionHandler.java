package com.usachevsergey.AssetBundleServer.security.exceptopionHandlers;

import com.usachevsergey.AssetBundleServer.exceptions.FieldNotFoundException;
import com.usachevsergey.AssetBundleServer.exceptions.VerifyEmailException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class PageExceptionHandler {
    @ExceptionHandler(FieldNotFoundException.class)
    public String handleFiledNotFoundException(FieldNotFoundException ex,
                                               Model model) {
        return generatePage(ex.getMessage(), model, ex.getStatusCode().value());
    }

    @ExceptionHandler(VerifyEmailException.class)
    public String handleVerifyEmailException(VerifyEmailException ex,
                                               Model model) {
        return generatePage(ex.getMessage(), model, ex.getStatusCode().value());
    }

    private String generatePage(String message, Model model, int errorCode) {
        model.addAttribute("status", "error");
        model.addAttribute("message", message);
        model.addAttribute("errorCode", errorCode);

        return "messagePage";
    }
}
