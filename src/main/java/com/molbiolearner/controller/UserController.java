package com.molbiolearner.controller;

import com.molbiolearner.util.UserIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Value("${app.admin-email:olsen.mark@gmail.com}")
    private String adminEmail;

    @GetMapping
    public ResponseEntity<?> currentUser(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Map<String, Object> info = new HashMap<>();
        info.put("name",     UserIdentity.displayName(user));
        info.put("email",    UserIdentity.email(user));
        info.put("avatar",   UserIdentity.avatar(user));
        info.put("userId",   UserIdentity.userId(user));
        info.put("provider", user.getAttribute("login") != null ? "github" : "google");
        info.put("isAdmin",  user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        return ResponseEntity.ok(info);
    }
}
