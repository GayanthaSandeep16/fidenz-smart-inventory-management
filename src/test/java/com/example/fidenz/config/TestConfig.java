package com.example.fidenz.config;

import com.example.fidenz.security.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {


    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
}
