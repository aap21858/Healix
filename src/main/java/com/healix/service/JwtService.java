package com.healix.service;

import com.healix.entity.Staff;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Staff staff) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", staff.getFullName());
        claims.put("role", staff.getRole());
        claims.put("emailId", staff.getEmailId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(staff.getEmailId())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmailId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, String emailId) {
        return emailId.equals(extractEmailId(token)) &&
                !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date exp = Jwts.parserBuilder().setSigningKey(secret.getBytes())
                .build().parseClaimsJws(token).getBody().getExpiration();
        return exp.before(new Date());
    }
}

