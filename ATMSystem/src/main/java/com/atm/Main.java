package com.atm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.atm")
@EnableJpaRepositories(basePackages = "com.atm.repository") // Đảm bảo quét các repository
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
