package com.atm.config;

import com.atm.util.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        logger.info("Configuring Security for ATM System...");

        http
                .csrf(csrf -> csrf.disable()) // Khi triển khai web, cần bật lại với csrf.withDefaults()
                .cors(cors -> {}) // Tùy chỉnh CORS theo nhu cầu
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/accounts/register", "/accounts/login", "/api/transactions/login").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/accounts/update", "/accounts/{accountNumber}/balance").authenticated()
                        .requestMatchers("/api/transactions/withdraw", "/api/transactions/transfer").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Thêm bộ lọc JWT

        logger.info("Security configuration completed.");
        return http.build();
    }
}