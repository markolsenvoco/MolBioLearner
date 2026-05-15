package com.molbiolearner.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "user_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "lessonId"}))
@Data
@NoArgsConstructor
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String lessonId;

    @Column(nullable = false)
    private String moduleId;

    private boolean completed;
    private Integer bestScore;
    private Instant completedAt;
    private Instant lastAttemptAt;

    public UserProgress(String userId, String lessonId, String moduleId) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.moduleId = moduleId;
        this.lastAttemptAt = Instant.now();
    }
}
