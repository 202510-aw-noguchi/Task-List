package com.example.todo.controller;

import com.example.todo.entity.AuditLog;
import com.example.todo.repository.AuditLogRepository;
import com.example.todo.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit-logs")
public class AdminAuditLogController {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AdminAuditLogController(AuditLogRepository auditLogRepository,
                                   UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String index(@RequestParam(name = "action", required = false) String action,
                        @RequestParam(name = "entityType", required = false) String entityType,
                        @RequestParam(name = "userId", required = false) Long userId,
                        @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                        Model model) {
        int safePage = Math.max(page, 1);
        Pageable pageable = PageRequest.of(safePage - 1, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (action != null && !action.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("action")), "%" + action.trim().toLowerCase() + "%"));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("entityType")), "%" + entityType.trim().toLowerCase() + "%"));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<AuditLog> result = auditLogRepository.findAll(spec, pageable);
        Set<Long> userIds = result.getContent().stream()
                .map(AuditLog::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = new LinkedHashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(user -> userNameMap.put(user.getId(), user.getUsername()));
        }
        model.addAttribute("logs", result.getContent());
        model.addAttribute("userNameMap", userNameMap);
        model.addAttribute("action", action);
        model.addAttribute("entityType", entityType);
        model.addAttribute("userId", userId);
        model.addAttribute("page", safePage);
        model.addAttribute("totalPages", Math.max(result.getTotalPages(), 1));
        model.addAttribute("totalCount", result.getTotalElements());
        return "admin/audit-logs";
    }
}
