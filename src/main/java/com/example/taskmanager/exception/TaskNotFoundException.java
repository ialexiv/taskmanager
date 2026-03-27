package com.example.taskmanager.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}