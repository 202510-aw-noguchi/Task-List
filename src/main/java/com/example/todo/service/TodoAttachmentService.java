package com.example.todo.service;

import com.example.todo.entity.TodoAttachment;
import com.example.todo.mapper.TodoAttachmentMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TodoAttachmentService {
    private final TodoAttachmentMapper todoAttachmentMapper;
    private final FileStorageService fileStorageService;

    public TodoAttachmentService(TodoAttachmentMapper todoAttachmentMapper, FileStorageService fileStorageService) {
        this.todoAttachmentMapper = todoAttachmentMapper;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public TodoAttachment attach(Long todoId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = fileStorageService.sanitizeFilename(file.getOriginalFilename());
        String stored = fileStorageService.save(file);
        if (stored == null) {
            return null;
        }
        TodoAttachment attachment = new TodoAttachment();
        attachment.setTodoId(todoId);
        attachment.setOriginalFilename(original);
        attachment.setStoredFilename(stored);
        attachment.setContentType(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setCreatedAt(LocalDateTime.now());
        try {
            todoAttachmentMapper.insert(attachment);
        } catch (RuntimeException ex) {
            fileStorageService.delete(stored);
            throw ex;
        }
        return attachment;
    }

    @Transactional(readOnly = true)
    public List<TodoAttachment> findByTodoId(Long todoId) {
        return todoAttachmentMapper.findByTodoId(todoId);
    }

    @Transactional(readOnly = true)
    public List<TodoAttachment> findByTodoIds(List<Long> todoIds) {
        if (todoIds == null || todoIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return todoAttachmentMapper.findByTodoIds(todoIds);
    }

    @Transactional(readOnly = true)
    public TodoAttachment findById(Long id) {
        return todoAttachmentMapper.findById(id);
    }

    public Resource loadAsResource(TodoAttachment attachment) throws IOException {
        return fileStorageService.loadAsResource(attachment.getStoredFilename());
    }

    @Transactional
    public void delete(Long attachmentId) throws IOException {
        TodoAttachment attachment = todoAttachmentMapper.findById(attachmentId);
        if (attachment == null) {
            return;
        }
        todoAttachmentMapper.deleteById(attachmentId);
        fileStorageService.delete(attachment.getStoredFilename());
    }
}
