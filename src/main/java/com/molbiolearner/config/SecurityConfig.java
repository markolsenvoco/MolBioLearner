package com.molbiolearner.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.dev-mode:true}")
    private boolean devMode;

    @Autowired
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;

    @Autowired
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // H2 console: must be logged in AND have ROLE_ADMIN (admin email only)
                .requestMatchers("/h2-console/**").hasRole("ADMIN")
                // Public
                .requestMatchers("/", "/index.html", "/js/**", "/content/**",
                                 "/api/modules/**", "/api/lessons/**", "/api/quiz/previous/**",
                                 "/actuator/health").permitAll()
                // Authenticated: progress and quiz attempts
                .requestMatchers("/api/progress/**", "/api/quiz/attempt/**").authenticated()
                .anyRequest().permitAll()
            );

        if (devMode) {
            http.csrf(csrf -> csrf
                    .ignoringRequestMatchers("/h2-console/**", "/api/**"))
                .headers(headers -> headers.frameOptions(f -> f.disable()));
        } else {
            http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        }

        http.oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(u -> u
                    .userService(oauth2UserService)
                    .oidcUserService(oidcUserService)))
            .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}
