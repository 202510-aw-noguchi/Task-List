package com.example.todo.aop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Component;

@Component
public class AopTraceStore {
    private static final int MAX_ENTRIES = 300;
    private final Deque<AopTraceEntry> entries = new ConcurrentLinkedDeque<>();

    public void addInfo(String method, String detail) {
        add("INFO", method, detail);
    }

    public void addError(String method, String detail) {
        add("ERROR", method, detail);
    }

    public List<AopTraceEntry> getRecent() {
        return new ArrayList<>(entries);
    }

    private void add(String level, String method, String detail) {
        entries.addFirst(new AopTraceEntry(
                LocalDateTime.now(),
                level,
                method,
                detail,
                Thread.currentThread().getName()
        ));
        while (entries.size() > MAX_ENTRIES) {
            entries.pollLast();
        }
    }
}
