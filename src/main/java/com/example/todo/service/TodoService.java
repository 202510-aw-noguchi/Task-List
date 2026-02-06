package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.entity.Category;
import com.example.todo.entity.Priority;
import com.example.todo.entity.TodoHistory;
import com.example.todo.exception.BusinessException;
import com.example.todo.form.TodoForm;
import com.example.todo.mapper.TodoMapper;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.TodoHistoryRepository;
import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TodoHistoryRepository todoHistoryRepository;
    private final AuditService auditService;
    private final AsyncTaskService asyncTaskService;

    public TodoService(TodoRepository todoRepository, TodoMapper todoMapper,
                       CategoryRepository categoryRepository, UserRepository userRepository,
                       TodoHistoryRepository todoHistoryRepository, AuditService auditService,
                       AsyncTaskService asyncTaskService) {
        this.todoRepository = todoRepository;
        this.todoMapper = todoMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.todoHistoryRepository = todoHistoryRepository;
        this.auditService = auditService;
        this.asyncTaskService = asyncTaskService;
    }

    @Transactional(rollbackFor = Exception.class, noRollbackFor = BusinessException.class)
    public Todo save(TodoForm form, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Todo todo = new Todo();
        todo.setAuthor(form.getAuthor());
        todo.setAssignee(form.getAssignee());
        todo.setTitle(form.getTitle());
        todo.setDetail(form.getDetail());
        todo.setPriority(form.getPriority() == null ? Priority.MEDIUM : form.getPriority());
        todo.setCategory(resolveCategory(form.getCategoryId()));
        todo.setStartDate(form.getStartDate());
        todo.setDeadline(form.getDeadline());
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);
        todo.setUser(resolveUser(userId));
        Todo saved = todoRepository.save(todo);

        TodoHistory history = new TodoHistory();
        history.setTodoId(saved.getId());
        history.setAction("CREATE");
        history.setDetail(saved.getTitle());
        todoHistoryRepository.save(history);

        auditService.record("CREATE", "Todo created", saved.getId(), userId);
        asyncTaskService.sendEmail(saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Todo> findAllByUserId(Long userId) {
        return todoRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Todo> searchByTitle(String keyword) {
        return todoRepository.searchByTitle(keyword);
    }

    @Transactional(readOnly = true)
    public List<Todo> findAllSorted(String keyword, Long categoryId, Long userId, String sort, String dir,
                                    int limit, int offset) {
        return todoMapper.findAllSorted(keyword, categoryId, userId, sort, dir, limit, offset);
    }

    @Transactional(readOnly = true)
    public long countAll(String keyword, Long categoryId, Long userId) {
        return todoMapper.countAll(keyword, categoryId, userId);
    }

    @Transactional(readOnly = true)
    public Todo findById(Long id, Long userId) {
        if (userId == null) {
            return todoRepository.findById(id).orElse(null);
        }
        return todoRepository.findByIdAndUser_Id(id, userId).orElse(null);
    }

    public Todo update(TodoForm form, Long userId) {
        Todo todo = findById(form.getId(), userId);
        if (todo == null) {
            return null;
        }
        todo.setAuthor(form.getAuthor());
        todo.setAssignee(form.getAssignee());
        todo.setTitle(form.getTitle());
        todo.setDetail(form.getDetail());
        todo.setPriority(form.getPriority() == null ? Priority.MEDIUM : form.getPriority());
        todo.setCategory(resolveCategory(form.getCategoryId()));
        todo.setStartDate(form.getStartDate());
        todo.setDeadline(form.getDeadline());
        todo.setUpdatedAt(LocalDateTime.now());
        return todoRepository.save(todo);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        if (userId == null) {
            todoRepository.deleteById(id);
            return;
        }
        todoRepository.deleteByIdAndUser_Id(id, userId);
    }

    public boolean toggleCompleted(Long id, Long userId) {
        Todo todo = findById(id, userId);
        if (todo == null) {
            return false;
        }
        todo.setCompleted(!todo.isCompleted());
        todo.setUpdatedAt(LocalDateTime.now());
        todoRepository.save(todo);
        return true;
    }

    @Transactional
    public int deleteByIds(List<Long> ids, Long userId) {
        return todoMapper.deleteByIds(ids, userId);
    }

    @Transactional(readOnly = true)
    public List<Todo> findAllForCsv(String keyword, Long categoryId, Long userId, String sort, String dir) {
        return todoMapper.findAllForCsv(keyword, categoryId, userId, sort, dir);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long todoId, String username) {
        if (todoId == null || username == null || username.isBlank()) {
            return false;
        }
        return todoRepository.existsByIdAndUser_Username(todoId, username);
    }

    @Transactional(readOnly = true)
    public AppUser findUserByUsername(String username) {
        Optional<AppUser> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId).orElse(null);
    }

    private AppUser resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }
}




