package com.molbiolearner.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molbiolearner.model.QuizAttempt;
import com.molbiolearner.model.QuizType;

import java.util.List;
import java.util.Map;

public interface QuizProcessor {
    boolean supports(QuizType type);

    QuizAttempt process(QuizAttempt attempt, List<Map<String, Object>> rawAnswers, ObjectMapper objectMapper);

    boolean requiresMarking();
}
