package com.molbiolearner.repository;

import com.molbiolearner.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByAttemptedAtDesc(String userId);
    List<QuizAttempt> findByUserIdAndLessonIdOrderByAttemptedAtDesc(String userId, String lessonId);
}
