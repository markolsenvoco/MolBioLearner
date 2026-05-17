package com.molbiolearner.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.molbiolearner.model.AttemptStatus;
import com.molbiolearner.model.QuizAnswer;
import com.molbiolearner.model.QuizAttempt;
import com.molbiolearner.model.QuizType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenEndedProcessor implements QuizProcessor {

    @Override
    public boolean supports(QuizType type) {
        return type == QuizType.OPEN_ENDED;
    }

    @Override
    public QuizAttempt process(QuizAttempt attempt, List<Map<String, Object>> rawAnswers, ObjectMapper objectMapper) {
        attempt.getAnswers().clear();

        for (Map<String, Object> rawAnswer : rawAnswers) {
            QuizAnswer answer = new QuizAnswer();
            answer.setQuestionId(stringValue(rawAnswer.get("questionId")));
            answer.setQuestionText(stringValue(rawAnswer.get("questionText")));
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "open_ended");
            data.put("question", stringValue(rawAnswer.get("questionText")));
            data.put("answer", stringValue(rawAnswer.get("text")));
            answer.setAnswerData(writeJson(objectMapper, data));
            answer.setScore(null);
            answer.setFeedback(null);
            attempt.addAnswer(answer);
        }

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setScore(null);
        return attempt;
    }

    @Override
    public boolean requiresMarking() {
        return true;
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String writeJson(ObjectMapper objectMapper, Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize open-ended answer", e);
        }
    }
}
