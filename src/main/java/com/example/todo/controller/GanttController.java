package com.example.todo.controller;

import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GanttController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final TodoRepository todoRepository;

    public GanttController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping("/gantt")
    public String ganttPage() {
        return "gantt";
    }

    @GetMapping("/gantt/data")
    @ResponseBody
    public List<GanttTaskDto> ganttData(@RequestParam("start") String start,
                                        @RequestParam("end") String end) {
        LocalDate rangeStart = LocalDate.parse(start, DATE_FORMAT);
        LocalDate rangeEnd = LocalDate.parse(end, DATE_FORMAT);
        return todoRepository.findAll().stream()
                .flatMap(todo -> toTask(todo, rangeStart, rangeEnd))
                .filter(Objects::nonNull)
                .toList();
    }

    private Stream<GanttTaskDto> toTask(Todo todo, LocalDate rangeStart, LocalDate rangeEnd) {
        if (todo.getCreatedAt() == null) {
            return Stream.empty();
        }
        LocalDate start = todo.getStartDate() != null
                ? todo.getStartDate()
                : todo.getCreatedAt().toLocalDate();
        LocalDate end = todo.getDeadline() != null ? todo.getDeadline() : start;
        if (end.isBefore(start)) {
            end = start;
        }
        if (end.isBefore(rangeStart) || start.isAfter(rangeEnd)) {
            return Stream.empty();
        }
        LocalDate clampedStart = start.isBefore(rangeStart) ? rangeStart : start;
        LocalDate clampedEnd = end.isAfter(rangeEnd) ? rangeEnd : end;

        String assignee = todo.getAssignee();
        if (assignee == null || assignee.isBlank()) {
            assignee = todo.getAuthor() == null ? "" : todo.getAuthor();
        }
        String groupName = todo.getCategory() != null ? todo.getCategory().getName() : "未設定";
        String groupColor = todo.getCategory() != null ? todo.getCategory().getColor() : null;
        String status = todo.getStatus() == null ? "NOT_STARTED" : todo.getStatus().name();
        String priority = todo.getPriority() != null ? todo.getPriority().name() : "MEDIUM";
        String name = groupName + " / " + todo.getTitle();
        return Stream.of(new GanttTaskDto(
                String.valueOf(todo.getId()),
                name,
                clampedStart.format(DATE_FORMAT),
                clampedEnd.format(DATE_FORMAT),
                0,
                assignee,
                status,
                priority,
                start.format(DATE_FORMAT),
                end.format(DATE_FORMAT),
                groupName,
                todo.getTitle(),
                groupColor
        ));
    }

    public record GanttTaskDto(
            String id,
            String name,
            String start,
            String end,
            int progress,
            String assignee,
            String status,
            String priority,
            String originalStart,
            String originalEnd,
            String groupName,
            String title,
            String groupColor
    ) {}
}
