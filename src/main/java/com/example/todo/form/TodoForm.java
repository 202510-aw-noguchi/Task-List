package com.example.todo.form;

import com.example.todo.entity.Priority;
import com.example.todo.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * ToDo入力フォームを表します。
 * <p>
 * 画面入力値を保持し、バリデーションを通して
 * {@link com.example.todo.service.TodoService} に受け渡します。
 *
 * @author Task-List Team
 * @version 1.0
 * @since 1.0
 * @see com.example.todo.entity.Todo
 */
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

    /**
     * フォームIDを返します。
     *
     * @return フォームID
     */
    public Long getId() {
        return id;
    }

    /**
     * フォームIDを設定します。
     *
     * @param id フォームID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 登録者を返します。
     *
     * @return 登録者
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 登録者を設定します。
     *
     * @param author 登録者
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 担当者を返します。
     *
     * @return 担当者
     */
    public String getAssignee() {
        return assignee;
    }

    /**
     * 担当者を設定します。
     *
     * @param assignee 担当者
     */
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    /**
     * タイトルを返します。
     *
     * @return タイトル
     */
    public String getTitle() {
        return title;
    }

    /**
     * タイトルを設定します。
     *
     * @param title タイトル
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 詳細を返します。
     *
     * @return 詳細
     */
    public String getDetail() {
        return detail;
    }

    /**
     * 詳細を設定します。
     *
     * @param detail 詳細
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * 優先度を返します。
     *
     * @return 優先度
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * 優先度を設定します。
     *
     * @param priority 優先度
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * ステータスを返します。
     *
     * @return ステータス
     */
    public Status getStatus() {
        return status;
    }

    /**
     * ステータスを設定します。
     *
     * @param status ステータス
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * カテゴリIDを返します。
     *
     * @return カテゴリID
     */
    public Long getCategoryId() {
        return categoryId;
    }

    /**
     * カテゴリIDを設定します。
     *
     * @param categoryId カテゴリID
     */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * カテゴリ名を返します。
     *
     * @return カテゴリ名
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * カテゴリ名を設定します。
     *
     * @param categoryName カテゴリ名
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * 開始日を返します。
     *
     * @return 開始日
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * 開始日を設定します。
     *
     * @param startDate 開始日
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * 期限日を返します。
     *
     * @return 期限日
     */
    public LocalDate getDeadline() {
        return deadline;
    }

    /**
     * 期限日を設定します。
     *
     * @param deadline 期限日
     */
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}

