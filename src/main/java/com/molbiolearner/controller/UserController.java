package com.molbiolearner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping
    public ResponseEntity<?> currentUser(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of(
            "name",   user.getAttribute("name") != null ? user.getAttribute("name") : user.getAttribute("login"),
            "login",  user.getAttribute("login"),
            "avatar", user.getAttribute("avatar_url")
        ));
    }
}
