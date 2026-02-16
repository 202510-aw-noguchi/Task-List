package com.example.todo.controller;

import com.example.todo.api.ApiResponse;
import com.example.todo.entity.Todo;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Status;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.form.TodoForm;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
public class TodoApiController {
    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Todo>>> list(Authentication authentication) {
        Long userId = resolveScopeUserId(authentication);
        List<Todo> todos;
        if (userId == null) {
            todos = todoService.findAll();
        } else {
            todos = todoService.findAllByUserId(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(todos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> get(@PathVariable("id") Long id, Authentication authentication) {
        Long userId = resolveScopeUserId(authentication);
        Todo todo = todoService.findById(id, userId);
        if (todo == null) {
            throw new TodoNotFoundException(id);
        }
        return ResponseEntity.ok(ApiResponse.success(todo));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Todo>> create(@Valid @RequestBody TodoForm form,
                                                    Authentication authentication) {
        Long userId = requireUserId(authentication);
        if (form.getPriority() == null) {
            form.setPriority(Priority.MEDIUM);
        }
        if (form.getStatus() == null) {
            form.setStatus(Status.NOT_STARTED);
        }
        Todo created = todoService.save(form, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> update(@PathVariable("id") Long id,
                                                    @Valid @RequestBody TodoForm form,
                                                    Authentication authentication) {
        Long userId = resolveScopeUserId(authentication);
        form.setId(id);
        Todo updated = todoService.update(form, userId);
        if (updated == null) {
            throw new TodoNotFoundException(id);
        }
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, Authentication authentication) {
        Long userId = resolveScopeUserId(authentication);
        Todo existing = todoService.findById(id, userId);
        if (existing == null) {
            throw new TodoNotFoundException(id);
        }
        todoService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            return null;
        }
        String username = userDetails.getUsername();
        com.example.todo.entity.AppUser user = todoService.findUserByUsername(username);
        return user != null ? user.getId() : null;
    }

    private Long resolveScopeUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (isAdmin) {
            return null;
        }
        return requireUserId(authentication);
    }
}
