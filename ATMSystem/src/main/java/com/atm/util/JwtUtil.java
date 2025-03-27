package com.atm.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final JwtSecretManager jwtSecretManager;

    public JwtUtil(JwtSecretManager jwtSecretManager) {
        this.jwtSecretManager = jwtSecretManager;
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtSecretManager.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String accountNumber, String role, long expirationTime) {
        return Jwts.builder()
                .setSubject(accountNumber)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            System.out.println("JWT validation failed: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // 🔹 Thêm phương thức extractClaim()
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
}