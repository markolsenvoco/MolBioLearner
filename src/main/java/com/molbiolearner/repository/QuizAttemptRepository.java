package com.molbiolearner.repository;

import com.molbiolearner.model.QuizAttempt;
import com.molbiolearner.model.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderBySubmittedAtDesc(String userId);

    List<QuizAttempt> findByUserIdAndLessonIdOrderByAttemptNumberDesc(String userId, String lessonId);

    Optional<QuizAttempt> findFirstByUserIdAndLessonIdOrderByAttemptNumberDesc(String userId, String lessonId);

    List<QuizAttempt> findByQuizTypeOrderBySubmittedAtDesc(QuizType quizType);

    List<QuizAttempt> findByLessonIdAndQuizTypeOrderBySubmittedAtDesc(String lessonId, QuizType quizType);

    @Query("SELECT COALESCE(MAX(a.attemptNumber), 0) FROM QuizAttempt a WHERE a.userId = ?1 AND a.lessonId = ?2")
    int findMaxAttemptNumber(String userId, String lessonId);

    @Query("SELECT a.userId, COUNT(a), MAX(a.submittedAt), MAX(a.userEmail) FROM QuizAttempt a GROUP BY a.userId ORDER BY a.userId")
    List<Object[]> findUserSummaries();

    @Query("SELECT DISTINCT a.moduleId, a.lessonId FROM QuizAttempt a WHERE a.userId = ?1 ORDER BY a.moduleId, a.lessonId")
    List<Object[]> findDistinctModuleAndLessonIdsByUserId(String userId);

    List<QuizAttempt> findByUserIdAndLessonIdOrderByAttemptNumberAsc(String userId, String lessonId);
}
