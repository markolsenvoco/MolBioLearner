package com.molbiolearner.controller;

import com.molbiolearner.service.ProgressService;
import com.molbiolearner.util.UserIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public ResponseEntity<?> getProgress(@AuthenticationPrincipal OAuth2User user) {
        String userId = UserIdentity.userId(user);
        return ResponseEntity.ok(Map.of(
            "progress", progressService.getProgress(userId),
            "summary", progressService.getSummary(userId)
        ));
    }

    @PostMapping("/lesson/{lessonId}/view")
    public ResponseEntity<?> markViewed(@AuthenticationPrincipal OAuth2User user,
                                         @PathVariable String lessonId,
                                         @RequestBody Map<String, String> body) {
        String moduleId = body.getOrDefault("moduleId", "unknown");
        return ResponseEntity.ok(progressService.markLessonViewed(UserIdentity.userId(user), lessonId, moduleId));
    }
}
