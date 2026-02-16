package com.example.todo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.todo.entity.TodoAttachment;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@MybatisTest
@MapperScan("com.example.todo.mapper")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TodoAttachmentMapperTest {
    @Autowired
    private TodoAttachmentMapper todoAttachmentMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO groups (id, name, color) VALUES (100, 'Test', '#000')");
        jdbcTemplate.update("INSERT INTO users (id, username, password, role) VALUES (100, 'test-user', 'x', 'ROLE_ADMIN')");
        jdbcTemplate.update(
                "INSERT INTO todos (id, author, assignee, title, detail, created_at, updated_at, status, priority, start_date, due_date, group_id, user_id) " +
                "VALUES (100, 'Admin', 'Admin', 'Test', 'Detail', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'NOT_STARTED', 'MEDIUM', NULL, NULL, 100, 100)");
    }

    @Test
    void insertAndFindByTodoId() {
        TodoAttachment attachment = new TodoAttachment();
        attachment.setTodoId(100L);
        attachment.setOriginalFilename("note.txt");
        attachment.setStoredFilename("uuid-file");
        attachment.setContentType("text/plain");
        attachment.setSize(12L);
        attachment.setCreatedAt(LocalDateTime.now());

        todoAttachmentMapper.insert(attachment);

        List<TodoAttachment> found = todoAttachmentMapper.findByTodoId(100L);
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getOriginalFilename()).isEqualTo("note.txt");
    }
}
