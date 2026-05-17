package com.molbiolearner.service;

import com.molbiolearner.model.QuizAttempt;
import com.molbiolearner.model.UserProgress;
import com.molbiolearner.repository.QuizAttemptRepository;
import com.molbiolearner.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository progressRepo;
    private final QuizAttemptRepository quizRepo;

    public List<UserProgress> getProgress(String userId) {
        return progressRepo.findByUserId(userId);
    }

    public UserProgress markLessonViewed(String userId, String lessonId, String moduleId) {
        return progressRepo.findByUserIdAndLessonId(userId, lessonId)
            .orElseGet(() -> progressRepo.save(new UserProgress(userId, lessonId, moduleId)));
    }

    public void updateProgressAfterQuiz(String userId, String lessonId, String moduleId, int score, int total, boolean passed) {
        UserProgress progress = progressRepo.findByUserIdAndLessonId(userId, lessonId)
            .orElse(new UserProgress(userId, lessonId, moduleId));
        if (progress.getBestScore() == null || score > progress.getBestScore()) {
            progress.setBestScore(score);
        }
        if (passed && !progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(Instant.now());
        }
        progress.setLastAttemptAt(Instant.now());
        progressRepo.save(progress);
    }

    public Map<String, Object> getSummary(String userId) {
        long totalCompleted = progressRepo.countByUserIdAndCompletedTrue(userId);
        List<QuizAttempt> attempts = quizRepo.findByUserIdOrderBySubmittedAtDesc(userId);
        double avgScore = attempts.stream()
            .filter(a -> a.getScore() != null && a.getTotalQuestions() != null && a.getTotalQuestions() > 0)
            .mapToDouble(a -> (double) a.getScore() / a.getTotalQuestions() * 100)
            .average()
            .orElse(0);
        return Map.of(
            "totalLessonsCompleted", totalCompleted,
            "totalQuizAttempts", attempts.size(),
            "averageScore", Math.round(avgScore)
        );
    }
}
