package com.example.taskmanager.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanager.dto.TaskCreateRequest;
import com.example.taskmanager.dto.TaskStatusUpdateRequest;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Description");
        task.setStatus(TaskStatus.NEW);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        createRequest = new TaskCreateRequest();
        createRequest.setTitle("New Task");
        createRequest.setDescription("New Description");
    }

    @Test
    void createTask_Success() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        StepVerifier.create(taskService.createTask(createRequest))
                .expectNextMatches(response -> response.getId().equals(1L) && response.getTitle()
                        .equals("Test Task") && response.getStatus() == TaskStatus.NEW)
                .verifyComplete();

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getTaskById_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        StepVerifier.create(taskService.getTaskById(1L))
                .expectNextMatches(response -> response.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getTaskById_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        StepVerifier.create(taskService.getTaskById(1L)).expectError(TaskNotFoundException.class).verify();
    }

    @Test
    void getTasks_PaginationAndFilter() {
        when(taskRepository.findAll(0, 10, TaskStatus.NEW)).thenReturn(List.of(task));
        when(taskRepository.count(TaskStatus.NEW)).thenReturn(1L);

        StepVerifier.create(taskService.getTasks(0, 10, TaskStatus.NEW))
                .expectNextMatches(page -> page.getContent()
                        .size() == 1 && page.getTotalElements() == 1 && page.getTotalPages() == 1)
                .verifyComplete();
    }

    @Test
    void updateStatus_Success() {
        TaskStatusUpdateRequest updateRequest = new TaskStatusUpdateRequest();
        updateRequest.setStatus(TaskStatus.DONE);

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Test Task");
        updatedTask.setDescription("Description");
        updatedTask.setStatus(TaskStatus.DONE);
        updatedTask.setCreatedAt(LocalDateTime.now());
        updatedTask.setUpdatedAt(LocalDateTime.now());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task), Optional.of(updatedTask));
        when(taskRepository.updateStatus(1L, TaskStatus.DONE)).thenReturn(1);

        StepVerifier.create(taskService.updateStatus(1L, updateRequest))
                .expectNextMatches(response -> response.getStatus() == TaskStatus.DONE)
                .verifyComplete();

        verify(taskRepository, times(2)).findById(1L);
        verify(taskRepository, times(1)).updateStatus(1L, TaskStatus.DONE);
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.deleteById(1L)).thenReturn(1);

        StepVerifier.create(taskService.deleteTask(1L)).verifyComplete();

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_NotFound() {
        when(taskRepository.deleteById(1L)).thenReturn(0);

        StepVerifier.create(taskService.deleteTask(1L)).expectError(TaskNotFoundException.class).verify();
    }
}