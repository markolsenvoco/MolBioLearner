package com.molbiolearner.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String lessonId;

    private int score;
    private int totalQuestions;
    private Instant attemptedAt;

    @Column(columnDefinition = "TEXT")
    private String answersJson;

    public QuizAttempt(String userId, String lessonId, int score, int totalQuestions, String answersJson) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.answersJson = answersJson;
        this.attemptedAt = Instant.now();
    }
}
