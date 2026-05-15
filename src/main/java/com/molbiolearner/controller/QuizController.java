package com.molbiolearner.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.molbiolearner.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final ProgressService progressService;
    private final ObjectMapper objectMapper;

    @PostMapping("/submit/{lessonId}")
    public ResponseEntity<?> submitQuiz(@AuthenticationPrincipal OAuth2User user,
                                         @PathVariable String lessonId,
                                         @RequestBody Map<String, Object> body) throws JsonProcessingException {
        String userId = userId(user);
        String moduleId = (String) body.getOrDefault("moduleId", "unknown");
        int score = (int) body.getOrDefault("score", 0);
        int total = (int) body.getOrDefault("total", 0);

        @SuppressWarnings("unchecked")
        List<Object> answers = (List<Object>) body.getOrDefault("answers", List.of());
        String answersJson = objectMapper.writeValueAsString(answers);

        return ResponseEntity.ok(
            progressService.submitQuiz(userId, lessonId, moduleId, score, total, answersJson)
        );
    }

    private String userId(OAuth2User user) {
        Object login = user.getAttribute("login");
        return login != null ? "github:" + login : "user:" + user.getName();
    }
}
