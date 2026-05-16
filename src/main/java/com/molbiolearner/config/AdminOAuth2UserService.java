package com.molbiolearner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Configuration
public class AdminOAuth2UserService {

    @Value("${app.admin-email:mark.olsen@gmail.com}")
    private String adminEmail;

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User user = delegate.loadUser(request);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            user.getAuthorities().forEach(a -> authorities.add(new SimpleGrantedAuthority(a.getAuthority())));

            // Grant ROLE_ADMIN if the user's email matches the admin email
            String email = user.getAttribute("email");
            if (adminEmail.equals(email)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            String nameAttr = request.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
            return new DefaultOAuth2User(authorities, user.getAttributes(), nameAttr);
        };
    }
}
