package com.example.todo.repository;

import com.example.todo.entity.TodoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoHistoryRepository extends JpaRepository<TodoHistory, Long> {
}
