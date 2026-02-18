package com.example.todo.aop;

import java.time.LocalDateTime;

public record AopTraceEntry(
        LocalDateTime timestamp,
        String level,
        String method,
        String detail,
        String threadName
) {
}
