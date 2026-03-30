package com.flowlist.repository;

import com.flowlist.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, String> {

    // All tasks for a user, newest first
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Safety check before mutating
    Optional<Task> findByIdAndUserId(String id, Long userId);

    void deleteByIdAndUserId(String id, Long userId);
}
