package com.example.todo.mapper;

import com.example.todo.entity.TodoAttachment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TodoAttachmentMapper {
    int insert(TodoAttachment attachment);

    TodoAttachment findById(@Param("id") Long id);

    List<TodoAttachment> findByTodoId(@Param("todoId") Long todoId);

    List<TodoAttachment> findByTodoIds(@Param("ids") List<Long> ids);

    int deleteById(@Param("id") Long id);
}
