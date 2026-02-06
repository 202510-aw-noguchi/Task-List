package com.example.todo.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);

    @Async("emailExecutor")
    public void sendEmail(Long todoId) {
        try {
            Thread.sleep(300);
            logger.info("Email sent for todoId={} at {}", todoId, LocalDateTime.now());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Email task interrupted", ex);
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<String> generateReport(Long userId) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(ex);
        }
        String result = "Report generated for userId=" + userId + " at " + LocalDateTime.now();
        return CompletableFuture.completedFuture(result);
    }
}
