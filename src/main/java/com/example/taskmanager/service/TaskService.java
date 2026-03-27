package com.example.taskmanager.service;

import com.example.taskmanager.dto.*;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Mono<TaskResponse> createTask(TaskCreateRequest request) {
        return Mono.fromCallable(() -> {
            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setStatus(TaskStatus.NEW);
            LocalDateTime now = LocalDateTime.now();
            task.setCreatedAt(now);
            task.setUpdatedAt(now);
            Task saved = taskRepository.save(task);
            return toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TaskResponse> getTaskById(Long id) {
        return Mono.fromCallable(() -> taskRepository.findById(id)
                        .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id)))
                .map(this::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PageResponse<TaskResponse>> getTasks(int page, int size, TaskStatus status) {
        return Mono.fromCallable(() -> {
            int offset = page * size;
            List<Task> tasks = taskRepository.findAll(offset, size, status);
            long total = taskRepository.count(status);
            int totalPages = (int) Math.ceil((double) total / size);
            List<TaskResponse> content = tasks.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return new PageResponse<>(content, page, size, total, totalPages);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TaskResponse> updateStatus(Long id, TaskStatusUpdateRequest request) {
        return Mono.fromCallable(() -> {
            Task existing = taskRepository.findById(id)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
            int updated = taskRepository.updateStatus(id, request.getStatus());
            if (updated == 0) {
                throw new TaskNotFoundException("Task not found with id: " + id);
            }
            Task updatedTask = taskRepository.findById(id)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found after update"));
            return toResponse(updatedTask);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteTask(Long id) {
        return Mono.fromRunnable(() -> {
            int deleted = taskRepository.deleteById(id);
            if (deleted == 0) {
                throw new TaskNotFoundException("Task not found with id: " + id);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}