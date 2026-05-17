package com.molbiolearner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molbiolearner.model.QuizAttempt;
import com.molbiolearner.repository.QuizAnswerRepository;
import com.molbiolearner.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private QuizAttemptRepository attemptRepo;

    @Autowired
    private QuizAnswerRepository answerRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        return attemptRepo.findUserSummaries().stream().map(row -> {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("userId", row[0]);
            info.put("attemptCount", row[1]);
            info.put("lastSeen", row[2]);
            return info;
        }).collect(Collectors.toList());
    }

    @GetMapping("/lessons")
    public List<Map<String, Object>> getLessons(@RequestParam String userId) {
        return attemptRepo.findDistinctModuleAndLessonIdsByUserId(userId).stream().map(row -> {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("moduleId", row[0]);
            info.put("lessonId", row[1]);
            return info;
        }).collect(Collectors.toList());
    }

    @GetMapping("/questions")
    public List<Map<String, Object>> getQuestions(@RequestParam String userId, @RequestParam String lessonId) {
        List<String> questionIds = answerRepo.findDistinctQuestionIdsByUserIdAndLessonId(userId, lessonId);
        return questionIds.stream().map(qId -> {
            List<com.molbiolearner.model.QuizAnswer> first = answerRepo.findByUserIdLessonIdAndQuestionId(userId, lessonId, qId);
            String questionText = first.isEmpty() ? qId : first.get(0).getQuestionText();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("questionId", qId);
            info.put("questionText", questionText);
            return info;
        }).collect(Collectors.toList());
    }

    @GetMapping("/answers")
    public List<Map<String, Object>> getAnswers(
            @RequestParam String userId,
            @RequestParam String lessonId,
            @RequestParam String questionId) {

        List<QuizAttempt> attempts = attemptRepo.findByUserIdAndLessonIdOrderByAttemptNumberAsc(userId, lessonId);

        return attempts.stream().map(attempt -> {
            List<Map<String, Object>> answers = attempt.getAnswers().stream()
                .filter(a -> "all".equals(questionId) || questionId.equals(a.getQuestionId()))
                .map(a -> {
                    Map<String, Object> ans = new LinkedHashMap<>();
                    ans.put("questionId", a.getQuestionId());
                    ans.put("questionText", a.getQuestionText());
                    ans.put("answerData", parseJsonSafely(a.getAnswerData()));
                    ans.put("score", a.getScore());
                    ans.put("feedback", a.getFeedback());
                    return ans;
                })
                .collect(Collectors.toList());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("attemptNumber", attempt.getAttemptNumber());
            result.put("submittedAt", attempt.getSubmittedAt());
            result.put("quizType", attempt.getQuizType());
            result.put("status", attempt.getStatus());
            result.put("score", attempt.getScore());
            result.put("totalQuestions", attempt.getTotalQuestions());
            result.put("answers", answers);
            return result;
        })
        .filter(r -> !((List<?>) r.get("answers")).isEmpty())
        .collect(Collectors.toList());
    }

    private Object parseJsonSafely(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
}
