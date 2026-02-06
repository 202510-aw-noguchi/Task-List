package com.example.todo.exception;

public class TodoNotFoundException extends RuntimeException {
    private final Long todoId;

    public TodoNotFoundException(Long todoId) {
        super("Todo not found. id=" + todoId);
        this.todoId = todoId;
    }

    public Long getTodoId() {
        return todoId;
    }
}
