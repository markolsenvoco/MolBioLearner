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
public class MultipleChoiceProcessor implements QuizProcessor {

    @Override
    public boolean supports(QuizType type) {
        return type == QuizType.MULTIPLE_CHOICE;
    }

    @Override
    public QuizAttempt process(QuizAttempt attempt, List<Map<String, Object>> rawAnswers, ObjectMapper objectMapper) {
        int totalCorrect = 0;
        attempt.getAnswers().clear();

        for (Map<String, Object> rawAnswer : rawAnswers) {
            int chosen = asInt(rawAnswer.get("chosen"));
            int correct = asInt(rawAnswer.get("correct"));
            boolean isCorrect = chosen == correct;
            if (isCorrect) {
                totalCorrect++;
            }

            QuizAnswer answer = new QuizAnswer();
            answer.setQuestionId(stringValue(rawAnswer.get("questionId")));
            answer.setQuestionText(stringValue(rawAnswer.get("questionText")));
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "multiple_choice");
            data.put("question", stringValue(rawAnswer.get("questionText")));
            data.put("options", rawAnswer.get("options"));
            data.put("chosen", chosen);
            data.put("correct", correct);
            data.put("isCorrect", isCorrect);
            data.put("explanation", rawAnswer.get("explanation"));
            answer.setAnswerData(writeJson(objectMapper, data));
            answer.setScore(isCorrect ? 1 : 0);
            attempt.addAnswer(answer);
        }

        attempt.setScore(totalCorrect);
        attempt.setStatus(AttemptStatus.MARKED);
        return attempt;
    }

    @Override
    public boolean requiresMarking() {
        return false;
    }

    private int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String writeJson(ObjectMapper objectMapper, Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize multiple-choice answer", e);
        }
    }
}
