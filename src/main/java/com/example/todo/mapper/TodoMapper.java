package com.example.todo.mapper;

import com.example.todo.entity.Todo;
import com.example.todo.dto.UserTaskSummary;
import com.example.todo.dto.MonthlyProgressSummary;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TodoMapper {
    List<Todo> findAllSorted(@Param("keyword") String keyword,
                             @Param("categoryId") Long categoryId,
                             @Param("userId") Long userId,
                             @Param("includeCompleted") boolean includeCompleted,
                             @Param("sort") String sort,
                             @Param("dir") String dir,
                             @Param("limit") int limit,
                             @Param("offset") int offset);

    long countAll(@Param("keyword") String keyword,
                  @Param("categoryId") Long categoryId,
                  @Param("userId") Long userId,
                  @Param("includeCompleted") boolean includeCompleted);

    int deleteByIds(@Param("ids") List<Long> ids,
                    @Param("userId") Long userId);

    List<Todo> findAllForCsv(@Param("keyword") String keyword,
                             @Param("categoryId") Long categoryId,
                             @Param("userId") Long userId,
                             @Param("sort") String sort,
                             @Param("dir") String dir);

    List<UserTaskSummary> findUserTaskSummaries(@Param("userIds") List<Long> userIds);

    MonthlyProgressSummary findMonthlyProgressSummary(@Param("userId") Long userId,
                                                      @Param("start") LocalDate start,
                                                      @Param("end") LocalDate end);
}
