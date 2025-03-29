package com.atm.config;

import com.atm.util.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                .authorizeRequests(auth -> auth
                        .requestMatchers("/error").permitAll() // Cho phép truy cập trang lỗi
                        .requestMatchers("/accounts/register", "/accounts/login", "/api/transactions/login").permitAll()
                        .requestMatchers("/api/transactions/withdraw/otp").permitAll() // Cho phép rút tiền bằng OTP mà không cần xác thực
                        .requestMatchers("/accounts/update").authenticated()
                        .requestMatchers("/accounts/balance").authenticated()
                        .requestMatchers("/accounts/{accountNumber}").authenticated()
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/accounts").hasAuthority("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/transactions/withdraw", "/api/transactions/transfer").hasRole("USER") // Sử dụng hasRole thay vì hasAuthority
                        .requestMatchers("/api/transactions/history").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Thêm bộ lọc JWT

        logger.info("Security configuration completed.");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .inMemoryAuthentication()
                .withUser("user").password(passwordEncoder.encode("password")).roles("USER")
                .and()
                .withUser("admin").password(passwordEncoder.encode("admin")).roles("ADMIN");
        return authenticationManagerBuilder.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
