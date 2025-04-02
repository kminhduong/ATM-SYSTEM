package com.atm.util;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class JwtSecretManager {
    private final String secretKey;

    public JwtSecretManager() {
        // Tạo secret key 256-bit an toàn
        this.secretKey = Base64.getEncoder().encodeToString(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded());
        System.out.println("🔐 New JWT Secret Key: " + secretKey);
    }

    public String getSecretKey() {
        return secretKey;
    }
}