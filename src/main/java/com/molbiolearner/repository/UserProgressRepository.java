package com.molbiolearner.repository;

import com.molbiolearner.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserId(String userId);
    Optional<UserProgress> findByUserIdAndLessonId(String userId, String lessonId);
    long countByUserIdAndCompletedTrue(String userId);
    long countByUserIdAndModuleIdAndCompletedTrue(String userId, String moduleId);
}
