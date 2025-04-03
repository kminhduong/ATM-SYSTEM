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
        System.out.println("🔍 Role from DB before creating JWT: " + role);

        String token = Jwts.builder()
                .setSubject(accountNumber)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();

        System.out.println("🔐 Token đã tạo: " + token);
        return token;
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
        Claims claims = extractAllClaims(token);
        Object roleObj = claims.get("role");
        String role = (roleObj != null) ? roleObj.toString() : null;
        System.out.println("🔍 Vai trò lấy từ JWT (fix): " + role);
        return role;
    }

    public String generateTransactionToken(String accountNumber, String role, double amount, String otp, long expirationTime) {
        // Tạo token với các thông tin giao dịch
        return Jwts.builder()
                .setSubject(accountNumber)
                .claim("role", role)
                .claim("amount", amount)
                .claim("otp", otp)
                .claim("type", "WITHDRAWAL") // Loại giao dịch
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Thời gian hết hạn
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}