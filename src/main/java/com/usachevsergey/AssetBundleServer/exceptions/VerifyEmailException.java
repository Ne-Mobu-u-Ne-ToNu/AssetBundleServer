package com.usachevsergey.AssetBundleServer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class VerifyEmailException extends ResponseStatusException {
    public VerifyEmailException(HttpStatus status, String errorMessage) {
        super(status, errorMessage);
    }
}
