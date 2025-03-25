package com.atm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/check-mysql")
    public String checkMySQLConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return "Kết nối MySQL thành công!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Kết nối MySQL thất bại: " + e.getMessage();
        }
    }
}
