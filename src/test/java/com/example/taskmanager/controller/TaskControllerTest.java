package com.example.taskmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.taskmanager.dto.PageResponse;
import com.example.taskmanager.dto.TaskCreateRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.dto.TaskStatusUpdateRequest;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TaskService taskService;

    @Test
    void createTask_ReturnsCreated() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("New Task");
        request.setDescription("Desc");

        TaskResponse response = new TaskResponse(1L, "New Task", "Desc", TaskStatus.NEW,
                LocalDateTime.now(), LocalDateTime.now());

        when(taskService.createTask(any(TaskCreateRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("New Task");
    }

    @Test
    void getTaskById_ReturnsOk() {
        TaskResponse response = new TaskResponse(1L, "Task", null, TaskStatus.NEW,
                LocalDateTime.now(), LocalDateTime.now());
        when(taskService.getTaskById(1L)).thenReturn(Mono.just(response));

        webTestClient.get().uri("/api/tasks/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1);
    }

    @Test
    void getTaskById_NotFound_Returns404() {
        when(taskService.getTaskById(99L)).thenReturn(Mono.error(new TaskNotFoundException("Task not found")));

        webTestClient.get().uri("/api/tasks/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTasks_WithPagination_ReturnsOk() {
        PageResponse<TaskResponse> page = new PageResponse<>(List.of(), 0, 10, 0, 0);
        when(taskService.getTasks(0, 10, null)).thenReturn(Mono.just(page));

        webTestClient.get().uri("/api/tasks?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page").isEqualTo(0);
    }

    @Test
    void updateStatus_ReturnsOk() {
        TaskStatusUpdateRequest updateRequest = new TaskStatusUpdateRequest();
        updateRequest.setStatus(TaskStatus.DONE);

        TaskResponse response = new TaskResponse(1L, "Task", null, TaskStatus.DONE,
                LocalDateTime.now(), LocalDateTime.now());
        when(taskService.updateStatus(eq(1L), any(TaskStatusUpdateRequest.class))).thenReturn(Mono.just(response));

        webTestClient.patch().uri("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("DONE");
    }

    @Test
    void deleteTask_ReturnsNoContent() {
        when(taskService.deleteTask(1L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/tasks/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void createTask_InvalidTitle_ReturnsBadRequest() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("ab");
        request.setDescription("desc");

        webTestClient.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}