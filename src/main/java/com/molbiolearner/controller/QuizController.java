package com.molbiolearner.controller;

import com.molbiolearner.model.QuizType;
import com.molbiolearner.service.QuizService;
import com.molbiolearner.util.UserIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/attempt/{lessonId}")
    public ResponseEntity<?> submitAttempt(@AuthenticationPrincipal OAuth2User user,
                                           @PathVariable String lessonId,
                                           @RequestBody Map<String, Object> body) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = UserIdentity.userId(user);
        String moduleId = String.valueOf(body.getOrDefault("moduleId", "unknown"));
        QuizType quizType = QuizType.valueOf(body.get("quizType").toString().toUpperCase());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.getOrDefault("answers", List.of());

        return ResponseEntity.ok(quizService.submit(userId, lessonId, moduleId, quizType, answers));
    }

    @GetMapping("/previous/{lessonId}")
    public ResponseEntity<?> getPrevious(@AuthenticationPrincipal OAuth2User user,
                                         @PathVariable String lessonId) {
        if (user == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(quizService.getLatestAttempt(UserIdentity.userId(user), lessonId).orElse(null));
    }
}
