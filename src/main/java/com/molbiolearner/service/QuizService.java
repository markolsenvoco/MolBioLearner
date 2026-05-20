package com.molbiolearner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molbiolearner.model.QuizAttempt;
import com.molbiolearner.model.QuizType;
import com.molbiolearner.quiz.QuizProcessor;
import com.molbiolearner.quiz.QuizProcessorFactory;
import com.molbiolearner.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizProcessorFactory quizProcessorFactory;
    private final ProgressService progressService;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuizAttempt submit(String userId, String userEmail, String lessonId, String moduleId, QuizType quizType,
                              List<Map<String, Object>> rawAnswers) {
        List<Map<String, Object>> answers = rawAnswers == null ? List.of() : rawAnswers;
        int nextAttemptNumber = quizAttemptRepository.findMaxAttemptNumber(userId, lessonId) + 1;

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setUserEmail(userEmail);
        attempt.setLessonId(lessonId);
        attempt.setModuleId(moduleId);
        attempt.setQuizType(quizType);
        attempt.setAttemptNumber(nextAttemptNumber);
        attempt.setSubmittedAt(Instant.now());
        attempt.setTotalQuestions(answers.size());

        QuizProcessor processor = quizProcessorFactory.getProcessor(quizType);
        processor.process(attempt, answers, objectMapper);

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        if (!processor.requiresMarking() && savedAttempt.getScore() != null && savedAttempt.getTotalQuestions() != null) {
            int total = savedAttempt.getTotalQuestions();
            boolean passed = total > 0 && savedAttempt.getScore() >= (int) Math.ceil(total * 0.7);
            progressService.updateProgressAfterQuiz(
                userId,
                lessonId,
                moduleId,
                savedAttempt.getScore(),
                total,
                passed
            );
        }
        return savedAttempt;
    }

    @Transactional(readOnly = true)
    public Optional<QuizAttempt> getLatestAttempt(String userId, String lessonId) {
        Optional<QuizAttempt> attempt = quizAttemptRepository.findFirstByUserIdAndLessonIdOrderByAttemptNumberDesc(userId, lessonId);
        attempt.ifPresent(value -> value.getAnswers().size());
        return attempt;
    }
}
