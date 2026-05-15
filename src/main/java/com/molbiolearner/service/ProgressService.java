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

    public UserProgress submitQuiz(String userId, String lessonId, String moduleId,
                                   int score, int total, String answersJson) {
        quizRepo.save(new QuizAttempt(userId, lessonId, score, total, answersJson));

        UserProgress progress = progressRepo.findByUserIdAndLessonId(userId, lessonId)
            .orElse(new UserProgress(userId, lessonId, moduleId));

        if (progress.getBestScore() == null || score > progress.getBestScore()) {
            progress.setBestScore(score);
        }
        boolean passed = score >= (int) Math.ceil(total * 0.7);
        if (passed && !progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(Instant.now());
        }
        progress.setLastAttemptAt(Instant.now());
        return progressRepo.save(progress);
    }

    public Map<String, Object> getSummary(String userId) {
        long totalCompleted = progressRepo.countByUserIdAndCompletedTrue(userId);
        List<QuizAttempt> attempts = quizRepo.findByUserIdOrderByAttemptedAtDesc(userId);
        double avgScore = attempts.stream()
            .mapToDouble(a -> a.getTotalQuestions() > 0 ? (double) a.getScore() / a.getTotalQuestions() * 100 : 0)
            .average().orElse(0);
        return Map.of(
            "totalLessonsCompleted", totalCompleted,
            "totalQuizAttempts", attempts.size(),
            "averageScore", Math.round(avgScore)
        );
    }
}
