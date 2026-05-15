package com.molbiolearner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.dev-mode:true}")
    private boolean devMode;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public: static assets and lesson/quiz content
                .requestMatchers("/", "/index.html", "/js/**", "/content/**",
                                 "/api/modules/**", "/api/lessons/**",
                                 "/h2-console/**", "/actuator/health").permitAll()
                // Authenticated: progress tracking (user-specific data)
                .requestMatchers("/api/progress/**", "/api/quiz/submit/**").authenticated()
                .anyRequest().permitAll()
            );

        if (devMode) {
            // Disable CSRF and frame options for H2 console in dev
            http.csrf(csrf -> csrf
                    .ignoringRequestMatchers("/h2-console/**", "/api/**"))
                .headers(headers -> headers.frameOptions(f -> f.disable()));
        } else {
            http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        }

        http.oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true))
            .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}
