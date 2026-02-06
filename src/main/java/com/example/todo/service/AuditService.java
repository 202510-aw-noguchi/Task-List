package com.example.todo.service;

import com.example.todo.entity.AuditLog;
import com.example.todo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String message, Long todoId, Long userId) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setMessage(message);
        log.setTodoId(todoId);
        log.setUserId(userId);
        auditLogRepository.save(log);
    }
}
