package com.molbiolearner.repository;

import com.molbiolearner.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    @Query("SELECT DISTINCT an.questionId FROM QuizAnswer an JOIN an.attempt a WHERE a.userId = ?1 AND a.lessonId = ?2 ORDER BY an.questionId")
    List<String> findDistinctQuestionIdsByUserIdAndLessonId(String userId, String lessonId);

    @Query("SELECT an FROM QuizAnswer an JOIN an.attempt a WHERE a.userId = ?1 AND a.lessonId = ?2 AND an.questionId = ?3 ORDER BY a.attemptNumber ASC")
    List<QuizAnswer> findByUserIdLessonIdAndQuestionId(String userId, String lessonId, String questionId);

    @Query("SELECT an FROM QuizAnswer an JOIN an.attempt a WHERE a.userId = ?1 AND a.lessonId = ?2 AND an.feedback IS NOT NULL ORDER BY a.attemptNumber ASC, an.questionId ASC")
    List<QuizAnswer> findFeedbackByUserIdAndLessonId(String userId, String lessonId);
}
