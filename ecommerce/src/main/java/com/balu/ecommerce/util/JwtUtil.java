package com.balu.ecommerce.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Generate token for a user
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email) //who this token belongs to
                .claim("role", role) // extra token inside the token
                .setIssuedAt(new Date()) // when created
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // when expire
                .signWith(getSigningKey()) // sign with secret
                .compact();
    }

    // Extract email from token
    public String extractEmail(String token) {
//        Claims claims = getClaims(token);
//        return claims.getSubject();
        return getClaims(token).getSubject();
    }

    // Extract role from token
    public String extractRole(String token) {
//        Claims claims = getClaims(token);
//        return claims.get("role", String.class);
        return getClaims(token).get("role", String.class);
    }

    // Check if token is valid
    public boolean isTokenValid(String token) {
        try {
            getClaims(token); // throws exception if invalid or expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Get all claims from token
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Build the signing key from secret
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
