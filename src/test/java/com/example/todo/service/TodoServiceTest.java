package com.example.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.todo.entity.AppUser;
import com.example.todo.entity.Category;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;
import com.example.todo.entity.TodoHistory;
import com.example.todo.form.TodoForm;
import com.example.todo.mapper.TodoMapper;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.repository.TodoHistoryRepository;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private TodoMapper todoMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoHistoryRepository todoHistoryRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private AsyncTaskService asyncTaskService;

    @InjectMocks
    private TodoService todoService;

    @Test
    void findAllSorted_usesMapper() {
        when(todoMapper.findAllSorted(null, null, 1L, false, "createdAt", "desc", 10, 0))
                .thenReturn(List.of(new Todo()));

        List<Todo> result = todoService.findAllSorted(null, null, 1L, false, "createdAt", "desc", 10, 0);

        assertThat(result).hasSize(1);
        verify(todoMapper).findAllSorted(null, null, 1L, false, "createdAt", "desc", 10, 0);
    }

    @Test
    void countAll_usesMapper() {
        when(todoMapper.countAll("k", null, 1L, false)).thenReturn(3L);

        long count = todoService.countAll("k", null, 1L, false);

        assertThat(count).isEqualTo(3L);
        verify(todoMapper).countAll("k", null, 1L, false);
    }

    @Test
    void save_persistsTodoHistory_andAudit() {
        TodoForm form = new TodoForm();
        form.setAuthor("Admin");
        form.setTitle("Test");
        form.setDetail("Detail");
        form.setPriority(Priority.MEDIUM);
        form.setCategoryId(1L);

        Category category = new Category();
        category.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        AppUser user = new AppUser();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Todo saved = new Todo();
        saved.setId(10L);
        when(todoRepository.save(org.mockito.Mockito.any(Todo.class))).thenReturn(saved);

        Todo result = todoService.save(form, 1L);

        assertThat(result.getId()).isEqualTo(10L);
        ArgumentCaptor<TodoHistory> historyCaptor = ArgumentCaptor.forClass(TodoHistory.class);
        verify(todoHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getTodoId()).isEqualTo(10L);
        verify(auditService).record("CREATE", "Todo created", 10L, 1L);
        verify(asyncTaskService).sendEmail(10L);
    }
}
