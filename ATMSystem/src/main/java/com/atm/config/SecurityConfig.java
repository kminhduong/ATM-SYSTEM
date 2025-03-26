package com.atm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Security for ATM System...");

        http
                .csrf(csrf -> csrf.disable()) // Khi triển khai web, cần bật lại với csrf.withDefaults()
                .cors(cors -> {}) // CORS có thể tùy chỉnh theo nhu cầu
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll() // Health check
                        .requestMatchers("/accounts/register", "/accounts/login").permitAll()
                        .requestMatchers("/api/transactions/withdraw", "/api/transactions/transfer")
                        .hasAuthority("ROLE_USER") // Cần quyền USER cho giao dịch
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN") // Admin chỉ dành cho quản trị viên
                        .anyRequest().authenticated() // Các request khác phải đăng nhập
                );

        logger.info("Security configuration completed.");
        return http.build();
    }
}