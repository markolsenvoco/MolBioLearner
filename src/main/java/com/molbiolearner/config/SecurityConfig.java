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

    @Value("${app.local-network:192.168.0.0/24}")
    private String localNetwork;

    @Autowired
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;

    @Autowired
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // H2 console: local network OR logged-in admin
                .requestMatchers("/h2-console/**")
                    .access((authSupplier, context) -> {
                        var req = context.getRequest();
                        if (isLocalNetwork(req.getRemoteAddr())) {
                            return new org.springframework.security.authorization.AuthorizationDecision(true);
                        }
                        var authentication = authSupplier.get();
                        return new org.springframework.security.authorization.AuthorizationDecision(
                            authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                        );
                    })
                // Public
                .requestMatchers("/", "/index.html", "/js/**", "/content/**",
                                 "/api/modules/**", "/api/lessons/**",
                                 "/actuator/health", "/api/debug/**").permitAll()
                // Local network: full access without login
                .requestMatchers("/api/progress/**", "/api/quiz/submit/**")
                    .access((authSupplier, context) -> {
                        if (isLocalNetwork(context.getRequest().getRemoteAddr())) {
                            return new org.springframework.security.authorization.AuthorizationDecision(true);
                        }
                        return new org.springframework.security.authorization.AuthorizationDecision(
                            authSupplier.get().isAuthenticated()
                        );
                    })
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

    private boolean isLocalNetwork(String remoteAddr) {
        if (remoteAddr == null) return false;
        return remoteAddr.equals("127.0.0.1")
            || remoteAddr.equals("0:0:0:0:0:0:0:1")
            || remoteAddr.startsWith("192.168.0.")
            || remoteAddr.startsWith("192.168.1.");
    }
}
