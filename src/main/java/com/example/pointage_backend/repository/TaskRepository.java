package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByProjectIdIn(List<Long> projectIds);
    void deleteByProjectId(Long projectId);
}
