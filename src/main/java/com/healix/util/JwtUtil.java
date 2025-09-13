package com.healix.util;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {
    private final String secret = "healthcare_jwt_secret"; // move to ENV in prod
    private long expiration = 1000 * 60 * 60; // 1 hour

    public String generateToken(String emailId) {
        return Jwts.builder()
                .setSubject(emailId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) &&
                !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date exp = Jwts.parserBuilder().setSigningKey(secret.getBytes())
                .build().parseClaimsJws(token).getBody().getExpiration();
        return exp.before(new Date());
    }
}

