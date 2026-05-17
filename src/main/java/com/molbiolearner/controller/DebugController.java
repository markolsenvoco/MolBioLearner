package com.molbiolearner.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/me")
    public Map<String, Object> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("authenticated", auth != null && auth.isAuthenticated());
        info.put("principal_type", auth != null ? auth.getPrincipal().getClass().getSimpleName() : "none");
        info.put("name", auth != null ? auth.getName() : "none");
        info.put("authorities", auth != null
            ? auth.getAuthorities().stream().map(Object::toString).collect(Collectors.toList())
            : "none");

        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauthUser) {
            info.put("attributes", oauthUser.getAttributes());
        }
        return info;
    }
}
