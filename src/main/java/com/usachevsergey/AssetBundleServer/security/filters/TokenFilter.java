package com.usachevsergey.AssetBundleServer.security.filters;

import com.usachevsergey.AssetBundleServer.security.authorization.JwtCookieManager;
import com.usachevsergey.AssetBundleServer.security.authorization.JwtCore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtCore jwtCore;
    @Autowired
    JwtCookieManager jwtCookieManager;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt;
        String username = null;
        UserDetails userDetails;
        UsernamePasswordAuthenticationToken auth;

        try {
            jwt = jwtCookieManager.extractToken(request);
            if (jwt == null) {
                SecurityContextHolder.clearContext();
            }
            if (jwt != null) {
                try {
                    // Если токен валидный и скоро кончится генерируем и сохраняем новый
                    if (jwtCore.isTokenExpiringSoon(jwt)) {
                        jwt = jwtCore.generateTokenFromExisting(jwt);

                        jwtCookieManager.saveToken(jwt, response);
                    }
                    // Если с токеном все ок, радуемся, если нет - выбрасывается исключение
                    username = jwtCore.getNameFromJwt(jwt);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    SecurityContextHolder.clearContext();
                }
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userDetails = userDetailsService.loadUserByUsername(username);
                    auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
