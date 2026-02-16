package com.example.todo.repository;

import com.example.todo.entity.Todo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByOrderByCreatedAtDesc();

    List<Todo> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("select t from Todo t where t.title like concat('%', :keyword, '%') order by t.createdAt desc")
    List<Todo> searchByTitle(@Param("keyword") String keyword);

    @Query("select t from Todo t join fetch t.user where t.status <> com.example.todo.entity.Status.COMPLETED and t.deadline = :deadline")
    List<Todo> findDueForReminder(@Param("deadline") LocalDate deadline);

    Optional<Todo> findByIdAndUser_Id(Long id, Long userId);

    void deleteByIdAndUser_Id(Long id, Long userId);

    boolean existsByIdAndUser_Username(Long id, String username);

    boolean existsByUser_Id(Long userId);

    boolean existsByCategory_Id(Long categoryId);
}
