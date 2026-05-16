package com.molbiolearner.util;

import org.springframework.security.oauth2.core.user.OAuth2User;

public class UserIdentity {

    public static String userId(OAuth2User user) {
        // GitHub provides "login", Google provides "sub"
        Object login = user.getAttribute("login");
        if (login != null) return "github:" + login;
        Object sub = user.getAttribute("sub");
        if (sub != null) return "google:" + sub;
        return "user:" + user.getName();
    }

    public static String displayName(OAuth2User user) {
        Object name = user.getAttribute("name");
        if (name != null) return name.toString();
        Object login = user.getAttribute("login");
        if (login != null) return login.toString();
        Object email = user.getAttribute("email");
        if (email != null) return email.toString();
        return "User";
    }

    public static String avatar(OAuth2User user) {
        Object avatar = user.getAttribute("avatar_url");   // GitHub
        if (avatar != null) return avatar.toString();
        Object picture = user.getAttribute("picture");     // Google
        if (picture != null) return picture.toString();
        return null;
    }
}
