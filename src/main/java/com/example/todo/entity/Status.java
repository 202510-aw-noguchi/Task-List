package com.example.todo.entity;

public enum Status {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    public Status next() {
        return switch (this) {
            case NOT_STARTED -> IN_PROGRESS;
            case IN_PROGRESS -> COMPLETED;
            case COMPLETED -> NOT_STARTED;
        };
    }
}
