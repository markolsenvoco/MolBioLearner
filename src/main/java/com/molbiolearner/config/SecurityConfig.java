package com.molbiolearner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.dev-mode:true}")
    private boolean devMode;

    @Value("${app.admin-email:mark.olsen@gmail.com}")
    private String adminEmail;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // H2 console: only accessible to the admin email
                .requestMatchers("/h2-console/**").access((authSupplier, context) -> {
                    var authentication = authSupplier.get();
                    if (!authentication.isAuthenticated()) return new AuthorizationDecision(false);
                    if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {
                        String email = oauthUser.getAttribute("email");
                        return new AuthorizationDecision(adminEmail.equals(email));
                    }
                    return new AuthorizationDecision(false);
                })
                // Public: static assets and lesson/quiz content
                .requestMatchers("/", "/index.html", "/js/**", "/content/**",
                                 "/api/modules/**", "/api/lessons/**",
                                 "/actuator/health").permitAll()
                // Authenticated: progress tracking
                .requestMatchers("/api/progress/**", "/api/quiz/submit/**").authenticated()
                .anyRequest().permitAll()
            );

        if (devMode) {
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
