package com.example.todo.controller;

import com.example.todo.entity.Priority;
import com.example.todo.entity.Status;
import com.example.todo.entity.Todo;
import com.example.todo.entity.TodoAttachment;
import com.example.todo.form.TodoForm;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.service.TodoAttachmentService;
import com.example.todo.service.TodoService;
import com.example.todo.service.AsyncTaskService;
import com.example.todo.aop.AopTraceStore;
import java.io.IOException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import com.example.todo.dto.MonthlyProgressSummary;

@Controller
public class TodoController {
    private final TodoService todoService;
    private final CategoryRepository categoryRepository;
    private final TodoAttachmentService todoAttachmentService;
    private final AsyncTaskService asyncTaskService;
    private final AopTraceStore aopTraceStore;

    public TodoController(TodoService todoService, CategoryRepository categoryRepository,
                          TodoAttachmentService todoAttachmentService, AsyncTaskService asyncTaskService,
                          AopTraceStore aopTraceStore) {
        this.todoService = todoService;
        this.categoryRepository = categoryRepository;
        this.todoAttachmentService = todoAttachmentService;
        this.asyncTaskService = asyncTaskService;
        this.aopTraceStore = aopTraceStore;
    }

    @GetMapping("/")
    public String index(@org.springframework.web.bind.annotation.RequestParam(name = "keyword", required = false)
                        String keyword,
                        @org.springframework.web.bind.annotation.RequestParam(name = "categoryId", required = false)
                        Long categoryId,
                        @org.springframework.web.bind.annotation.RequestParam(name = "sort", required = false)
                        String sort,
                        @org.springframework.web.bind.annotation.RequestParam(name = "dir", required = false)
                        String dir,
                        @org.springframework.web.bind.annotation.RequestParam(name = "includeCompleted", required = false)
                        Boolean includeCompleted,
                        @org.springframework.web.bind.annotation.RequestParam(name = "showAllCompleted", required = false)
                        Boolean showAllCompleted,
                        @org.springframework.web.bind.annotation.RequestParam(name = "page", required = false)
                        Integer page,
                        Model model,
                        @AuthenticationPrincipal UserDetails userDetails,
                        Authentication authentication) {
        Long userId = requireUserId(userDetails);
        Long scopeUserId = isAdmin(authentication) ? null : userId;
        return renderIndex(keyword, categoryId, sort, dir, includeCompleted, showAllCompleted, page, model, scopeUserId, "index");
    }

    @GetMapping("/admin/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTodos(@org.springframework.web.bind.annotation.RequestParam(name = "keyword", required = false)
                             String keyword,
                             @org.springframework.web.bind.annotation.RequestParam(name = "categoryId", required = false)
                             Long categoryId,
                             @org.springframework.web.bind.annotation.RequestParam(name = "sort", required = false)
                             String sort,
                             @org.springframework.web.bind.annotation.RequestParam(name = "dir", required = false)
                             String dir,
                             @org.springframework.web.bind.annotation.RequestParam(name = "includeCompleted", required = false)
                             Boolean includeCompleted,
                             @org.springframework.web.bind.annotation.RequestParam(name = "page", required = false)
                             Integer page,
                             Model model) {
        return renderIndex(keyword, categoryId, sort, dir, includeCompleted, null, page, model, null, "admin/todos");
    }

    @GetMapping("/create")
    public String edit(Model model) {
        TodoForm form = new TodoForm();
        form.setPriority(Priority.MEDIUM);
        form.setStatus(Status.NOT_STARTED);
        model.addAttribute("todoForm", form);
        model.addAttribute("categories", categoryRepository.findAll());
        return "edit";
    }

    @GetMapping("/revision/{id}")
    @PreAuthorize("hasRole('ADMIN') or @todoService.isOwner(#p0, authentication.name)")
    public String editById(@PathVariable("id") Long id, Model model,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Authentication authentication) {
        Long userId = requireUserId(userDetails);
        Long scopeUserId = isAdmin(authentication) ? null : userId;
        Todo todo = todoService.findById(id, scopeUserId);
        if (todo == null) {
            throw new com.example.todo.exception.TodoNotFoundException(id);
        }
        TodoForm form = new TodoForm();
        form.setId(todo.getId());
        form.setAuthor(todo.getAuthor());
        form.setAssignee(todo.getAssignee());
        form.setTitle(todo.getTitle());
        form.setDetail(todo.getDetail());
        form.setPriority(todo.getPriority());
        form.setStatus(todo.getStatus() == null ? Status.NOT_STARTED : todo.getStatus());
        form.setStartDate(todo.getStartDate());
        form.setDeadline(todo.getDeadline());
        if (todo.getCategory() != null) {
            form.setCategoryId(todo.getCategory().getId());
            form.setCategoryName(todo.getCategory().getName());
        }
        model.addAttribute("todoForm", form);
        model.addAttribute("categories", categoryRepository.findAll());
        return "edit";
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('ADMIN') or (#p0 != null and (#p0.id == null or @todoService.isOwner(#p0.id, authentication.name)))")
    public String confirm(@Valid @ModelAttribute("todoForm") TodoForm form,
                          BindingResult bindingResult,
                          Model model,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Authentication authentication) {
        Long userId = requireUserId(userDetails);
        Long scopeUserId = isAdmin(authentication) ? null : userId;
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "edit";
        }
        if (form.getId() != null) {
            Todo existing = todoService.findById(form.getId(), scopeUserId);
            if (existing == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            java.util.List<TodoAttachment> attachments = todoAttachmentService.findByTodoId(form.getId());
            model.addAttribute("attachments", attachments);
        }
        Long categoryId = form.getCategoryId();
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(
                    category -> form.setCategoryName(category.getName()));
        } else {
            form.setCategoryName("\u672A\u9078\u629E");
        }
        return "confirm";
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('ADMIN') or (#p0 != null and (#p0.id == null or @todoService.isOwner(#p0.id, authentication.name)))")
    public String complete(@Valid @ModelAttribute("todoForm") TodoForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(name = "attachment", required = false) MultipartFile attachment,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Authentication authentication) {
        Long userId = requireUserId(userDetails);
        Long scopeUserId = isAdmin(authentication) ? null : userId;
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "edit";
        }
        Long categoryId = form.getCategoryId();
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(
                    category -> form.setCategoryName(category.getName()));
        } else {
            form.setCategoryName("\u672A\u9078\u629E");
        }
        if (form.getId() == null) {
            Todo created = todoService.save(form, userId);
            form.setId(created.getId());
        } else {
            Todo updated = todoService.update(form, scopeUserId);
            if (updated == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        if (attachment != null && !attachment.isEmpty() && form.getId() != null) {
            try {
                todoAttachmentService.attach(form.getId(), attachment);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to store attachment.", ex);
            }
        }
        redirectAttributes.addFlashAttribute("todoForm", form);
        return "redirect:/complete";
    }

    @GetMapping("/complete")
    public String completeView(Model model) {
        if (!model.containsAttribute("todoForm")) {
            TodoForm form = new TodoForm();
            form.setPriority(Priority.MEDIUM);
            form.setStatus(Status.NOT_STARTED);
            model.addAttribute("todoForm", form);
        }
        TodoForm form = (TodoForm) model.getAttribute("todoForm");
        if (form != null && form.getId() != null) {
            java.util.List<TodoAttachment> attachments = todoAttachmentService.findByTodoId(form.getId());
            model.addAttribute("attachments", attachments);
        }
        return "complete";
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable("id") Long id) {
        TodoAttachment attachment = todoAttachmentService.findById(id);
        if (attachment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        try {
            Resource resource = todoAttachmentService.loadAsResource(attachment);
            ContentDisposition disposition = ContentDisposition.attachment()
                    .filename(attachment.getOriginalFilename(), StandardCharsets.UTF_8)
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/attachments/{id}/delete")
    public String deleteAttachment(@PathVariable("id") Long id,
                                   @RequestParam("todoId") Long todoId,
                                   RedirectAttributes redirectAttributes) {
        try {
            todoAttachmentService.delete(id);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete attachment.", ex);
        }
        Todo todo = todoService.findById(todoId, null);
        if (todo != null) {
            TodoForm form = new TodoForm();
            form.setId(todo.getId());
            form.setAuthor(todo.getAuthor());
            form.setAssignee(todo.getAssignee());
            form.setTitle(todo.getTitle());
            form.setDetail(todo.getDetail());
            form.setPriority(todo.getPriority());
            form.setStatus(todo.getStatus() == null ? Status.NOT_STARTED : todo.getStatus());
            if (todo.getCategory() != null) {
                form.setCategoryId(todo.getCategory().getId());
                form.setCategoryName(todo.getCategory().getName());
            }
            form.setStartDate(todo.getStartDate());
            form.setDeadline(todo.getDeadline());
            redirectAttributes.addFlashAttribute("todoForm", form);
        }
        return "redirect:/complete";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or @todoService.isOwner(#p0, authentication.name)")
    public String delete(@PathVariable("id") Long id,
                         @RequestParam(name = "returnTo", required = false) String returnTo,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Authentication authentication) {
        Long userId = requireUserId(userDetails);
        Long scopeUserId = isAdmin(authentication) ? null : userId;
        Todo existing = todoService.findById(id, scopeUserId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        todoService.delete(id, scopeUserId);
        return "redirect:" + resolveReturnTo(returnTo);
    }

    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN') or @todoService.isOwner(#p0, authentication.name)")
    public String toggle(@PathVariable("id") Long id,
                         @RequestParam(name = "returnTo", required = false) String returnTo,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Authentication authentication) {
        Long userId = requireUserId(userDetails);
        Long scopeUserId = isAdmin(authentication) ? null : userId;
        boolean updated = todoService.advanceStatus(id, scopeUserId);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return "redirect:" + resolveReturnTo(returnTo);
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or @todoService.isOwner(#p0, authentication.name)")
    public ResponseEntity<Void> updateStatus(@PathVariable("id") Long id,
                                             @RequestParam("status") Status status,
                                             @AuthenticationPrincipal UserDetails userDetails,
                                             Authentication authentication) {
        Long userId = requireUserId(userDetails);
        Long scopeUserId = isAdmin(authentication) ? null : userId;
        boolean updated = todoService.updateStatus(id, status, scopeUserId);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSelected(@RequestParam(name = "ids", required = false) java.util.List<Long> ids,
                                 @RequestParam(name = "returnTo", required = false) String returnTo,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        if (ids != null && !ids.isEmpty()) {
            todoService.deleteByIds(ids, null);
        }
        return "redirect:" + resolveReturnTo(returnTo);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(name = "keyword", required = false) String keyword,
                                            @RequestParam(name = "categoryId", required = false) Long categoryId,
                                            @RequestParam(name = "sort", required = false) String sort,
                                            @RequestParam(name = "dir", required = false) String dir,
                                            @AuthenticationPrincipal UserDetails userDetails,
                                            Authentication authentication) {
        Long userId = requireUserId(userDetails);
        Long scopeUserId = isAdmin(authentication) ? null : userId;
        String normalizedSort = (sort == null || sort.isBlank()) ? "createdAt" : sort;
        String normalizedDir = (dir == null || dir.isBlank()) ? "desc" : dir;
        java.util.List<Todo> todos =
                todoService.findAllForCsv(keyword, categoryId, scopeUserId, normalizedSort, normalizedDir);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        StringBuilder sb = new StringBuilder();
        sb.append("ID,\u30bf\u30a4\u30c8\u30eb,\u767b\u9332\u8005,\u30b9\u30c6\u30fc\u30bf\u30b9,\u4f5c\u6210\u65e5").append("\r\n");
        for (Todo todo : todos) {
            sb.append(csv(todo.getId()))
              .append(',').append(csv(todo.getTitle()))
              .append(',').append(csv(todo.getAuthor()))
              .append(',').append(csv(statusLabel(todo.getStatus())))
              .append(',').append(csv(todo.getCreatedAt() != null ? dtf.format(todo.getCreatedAt()) : ""))
              .append("\r\n");
        }

        byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);

        String filename = "todo_" + java.time.LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(out);
    }

    private static String csv(Object value) {
        String s = value == null ? "" : value.toString();
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (needQuote) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private static String statusLabel(Status status) {
        if (status == null) {
            return "\u672A\u7740\u624B";
        }
        return switch (status) {
            case NOT_STARTED -> "\u672A\u7740\u624B";
            case IN_PROGRESS -> "\u5BFE\u5FDC\u4E2D";
            case COMPLETED -> "\u5B8C\u4E86";
        };
    }

    private Long requireUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        String username = userDetails.getUsername();
        com.example.todo.entity.AppUser user = todoService.findUserByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return user.getId();
    }

    private String renderIndex(String keyword, Long categoryId, String sort, String dir, Boolean includeCompleted,
                               Boolean showAllCompleted, Integer page, Model model,
                               Long userId, String viewName) {
        String normalizedSort = (sort == null || sort.isBlank()) ? "createdAt" : sort;
        String normalizedDir = (dir == null || dir.isBlank()) ? "desc" : dir;
        boolean showCompleted = includeCompleted != null ? includeCompleted : "index".equals(viewName);
        boolean showAllCompletedCards = showAllCompleted != null && showAllCompleted;
        int currentPage = (page == null || page < 1) ? 1 : page;
        long totalCount = todoService.countAll(keyword, categoryId, userId, showCompleted);
        int size = "index".equals(viewName) ? (int) Math.max(1L, Math.min(totalCount, Integer.MAX_VALUE)) : 10;
        int totalPages = (int) Math.max(1, (totalCount + size - 1) / size);
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        int offset = (currentPage - 1) * size;

        java.util.List<Todo> todos =
                todoService.findAllSorted(keyword, categoryId, userId, showCompleted,
                        normalizedSort, normalizedDir, size, offset);
        if ("index".equals(viewName) && !showAllCompletedCards) {
            LocalDateTime cutoff = LocalDateTime.now().minusWeeks(2);
            todos = todos.stream()
                    .filter(todo -> {
                        Status status = todo.getStatus() == null ? Status.NOT_STARTED : todo.getStatus();
                        if (status != Status.COMPLETED) {
                            return true;
                        }
                        LocalDateTime updatedAt = todo.getUpdatedAt();
                        return updatedAt == null || !updatedAt.isBefore(cutoff);
                    })
                    .toList();
        }
        model.addAttribute("todos", todos);
        java.util.List<Long> todoIds = todos.stream()
                .map(Todo::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        java.util.List<TodoAttachment> attachments = todoAttachmentService.findByTodoIds(todoIds);
        java.util.Map<Long, java.util.List<TodoAttachment>> attachmentsMap =
                attachments.stream().collect(java.util.stream.Collectors.groupingBy(TodoAttachment::getTodoId));
        model.addAttribute("attachmentsMap", attachmentsMap);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sort", normalizedSort);
        model.addAttribute("dir", normalizedDir);
        model.addAttribute("includeCompleted", showCompleted);
        model.addAttribute("showAllCompletedCards", showAllCompletedCards);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        int start = totalCount == 0 ? 0 : offset + 1;
        int end = (int) Math.min(totalCount, (long) offset + size);
        model.addAttribute("rangeStart", start);
        model.addAttribute("rangeEnd", end);
        model.addAttribute("categories", categoryRepository.findAll());

        YearMonth month = YearMonth.now();
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        MonthlyProgressSummary summary = todoService.findMonthlyProgressSummary(userId, monthStart, monthEnd);
        if (summary == null) {
            summary = new MonthlyProgressSummary();
        }
        long monthlyTotal = summary.getTotalCount();
        long monthlyNotStarted = summary.getNotStartedCount();
        long monthlyInProgress = summary.getInProgressCount();
        long monthlyCompleted = summary.getCompletedCount();
        double monthlyNotStartedRate = monthlyTotal == 0 ? 0.0 : (monthlyNotStarted * 100.0) / monthlyTotal;
        double monthlyInProgressRate = monthlyTotal == 0 ? 0.0 : (monthlyInProgress * 100.0) / monthlyTotal;
        double monthlyRate = monthlyTotal == 0 ? 0.0 : (monthlyCompleted * 100.0) / monthlyTotal;
        double monthlyNotStartedEnd = monthlyNotStartedRate;
        double monthlyInProgressEnd = monthlyNotStartedRate + monthlyInProgressRate;
        model.addAttribute("monthlyTotalCount", monthlyTotal);
        model.addAttribute("monthlyNotStartedCount", monthlyNotStarted);
        model.addAttribute("monthlyInProgressCount", monthlyInProgress);
        model.addAttribute("monthlyCompletedCount", monthlyCompleted);
        model.addAttribute("monthlyNotStartedEnd", monthlyNotStartedEnd);
        model.addAttribute("monthlyInProgressEnd", monthlyInProgressEnd);
        model.addAttribute("monthlyCompletedRate", monthlyRate);
        model.addAttribute("monthlyCompletedRateText",
                String.format(Locale.JAPAN, "%.1f", monthlyRate));
        return viewName;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String resolveReturnTo(String returnTo) {
        if ("/admin/todos".equals(returnTo)) {
            return returnTo;
        }
        return "/";
    }

    @GetMapping("/admin/report")
    @PreAuthorize("hasRole('ADMIN')")
    public String generateReport(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Long userId = requireUserId(userDetails);
        try {
            java.util.concurrent.CompletableFuture<String> future = asyncTaskService.generateReport(userId);
            String result = future.get(2, java.util.concurrent.TimeUnit.SECONDS);
            model.addAttribute("reportResult", result);
        } catch (java.util.concurrent.TimeoutException ex) {
            model.addAttribute("reportError", "Report generation timed out");
        } catch (Exception ex) {
            model.addAttribute("reportError", "Report generation failed");
        }
        model.addAttribute("aopLogs", aopTraceStore.getRecent());
        return "admin/report";
    }
}
