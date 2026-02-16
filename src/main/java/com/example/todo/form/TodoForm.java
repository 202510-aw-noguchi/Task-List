package com.example.todo.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Status;
import java.time.LocalDate;

public class TodoForm {
    private Long id;

    @NotBlank(message = "{validation.author.notBlank}")
    @Size(max = 50, message = "{validation.author.size}")
    private String author;

    @Size(max = 50)
    private String assignee;

    @NotBlank(message = "{validation.title.notBlank}")
    @Size(max = 100, message = "{validation.title.size}")
    private String title;

    @NotBlank(message = "{validation.detail.notBlank}")
    @Size(max = 1000, message = "{validation.detail.size}")
    private String detail;

    private Priority priority;
    private Status status = Status.NOT_STARTED;

    private Long categoryId;

    private String categoryName;

    private LocalDate startDate;

    private LocalDate deadline;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
