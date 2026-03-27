package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(Long id);
    List<Task> findAll(int offset, int limit, TaskStatus status);
    long count(TaskStatus status);
    int updateStatus(Long id, TaskStatus status);
    int deleteById(Long id);
}