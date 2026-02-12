package com.usachevsergey.AssetBundleServer.annotations.aspects;

import com.usachevsergey.AssetBundleServer.exceptions.VerifyEmailException;
import com.usachevsergey.AssetBundleServer.security.authorization.UserDetailsImpl;
import com.usachevsergey.AssetBundleServer.database.repositories.UserRepository;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class EmailVerifiedAspect {
    @Autowired
    private UserRepository userRepository;

    @Before("@annotation(com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly) || " +
            "@within(com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly)")
    public void checkEmailVerified() {
        User user = extractUser();

        if (!user.isEmailVerified()) {
            throw new VerifyEmailException(HttpStatus.PRECONDITION_REQUIRED, "Подтвердите адрес электронной почты!");
        }
    }

    private User extractUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userRepository.findUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден!"));
        }

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey != null && !apiKey.isBlank()) {
            return userRepository.findUserByApiKey(apiKey)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден!"));
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден!");
    }
}
