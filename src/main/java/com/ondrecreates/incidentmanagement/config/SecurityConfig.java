package com.ondrecreates.incidentmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Prozatímní permissive konfigurace. spring-boot-starter-oauth2-resource-server
 * je na classpath už od Fáze 1A (viz FAZE_1_PROMPT.md), takže bez vlastního
 * SecurityFilterChain by Spring Boot automaticky zamkl všechny endpointy
 * generovaným basic-auth heslem — to by shodilo REST API testy z Fáze 1C.
 * Fáze 1D tuhle třídu nahradí skutečným OAuth2 Resource Server nastavením
 * (JWT validace proti identity_server_app, actorUserId z claimu).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
