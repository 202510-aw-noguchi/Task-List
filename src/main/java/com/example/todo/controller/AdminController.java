package com.example.todo.controller;

import com.example.todo.repository.UserRepository;
import com.example.todo.mapper.TodoMapper;
import com.example.todo.dto.UserTaskSummary;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final TodoMapper todoMapper;

    public AdminController(UserRepository userRepository, TodoMapper todoMapper) {
        this.userRepository = userRepository;
        this.todoMapper = todoMapper;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String users(Model model) {
        List<com.example.todo.entity.AppUser> users = userRepository.findAll();
        List<Long> userIds = users.stream()
                .map(com.example.todo.entity.AppUser::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        List<UserTaskSummary> summaries = userIds.isEmpty()
                ? java.util.List.of()
                : todoMapper.findUserTaskSummaries(userIds);
        Map<Long, UserTaskSummary> summaryMap = summaries.stream()
                .collect(Collectors.toMap(UserTaskSummary::getUserId, s -> s));
        model.addAttribute("users", users);
        model.addAttribute("summaryMap", summaryMap);
        return "admin/users";
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String index() {
        return "admin/index";
    }
}
