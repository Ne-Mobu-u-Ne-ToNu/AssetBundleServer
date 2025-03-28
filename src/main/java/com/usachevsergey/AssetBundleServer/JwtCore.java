package com.usachevsergey.AssetBundleServer;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtCore {
    @Value("${server.app.secret}")
    private String secret;
    @Value("${server.app.lifetime}")
    private int lifetime;
    @Value("${server.app.tokenThreshold}")
    private int threshold;

    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder().setSubject((userDetails.getUsername())).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + lifetime * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getNameFromJwt(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        Jws<Claims> jwsClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

        return jwsClaims.getBody().getSubject();
    }

    public boolean isTokenExpiringSoon(String token) {
        Claims claims = getClaims(token);
        long expirationTime = claims.getExpiration().getTime();
        long currentTime = System.currentTimeMillis();
        long dif = expirationTime - currentTime;
        return dif < threshold * 1000L && dif >= 0;
    }

    public String generateTokenFromExisting(String oldToken) {
        return generateTokenFromClaims(getClaims(oldToken));
    }

    public int getLifetime() {
        return this.lifetime;
    }

    private String generateTokenFromClaims(Claims claims) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date((new Date()).getTime() + lifetime * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
