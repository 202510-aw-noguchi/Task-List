package com.example.todo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ToDoを表すエンティティです。
 * <p>
 * DBテーブル {@code todos} と対応し、業務上の状態・優先度・担当情報を保持します。
 *
 * @author Task-List Team
 * @version 1.0
 * @since 1.0
 * @see Status
 */
@Entity
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String author;

    @Column
    private String assignee;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private AppUser user;

    @Transient
    private Long categoryId;

    @Transient
    private String categoryName;

    @Transient
    private String categoryColor;

    @Transient
    private Long userId;

    /**
     * IDを返します。
     *
     * @return ToDoのID
     */
    public Long getId() {
        return id;
    }

    /**
     * IDを設定します。
     *
     * @param id ToDoのID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 登録者を返します。
     *
     * @return 登録者名
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 登録者を設定します。
     *
     * @param author 登録者名
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 担当者を返します。
     *
     * @return 担当者名
     */
    public String getAssignee() {
        return assignee;
    }

    /**
     * 担当者を設定します。
     *
     * @param assignee 担当者名
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
     * 作成日時を返します。
     *
     * @return 作成日時
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 作成日時を設定します。
     *
     * @param createdAt 作成日時
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 更新日時を返します。
     *
     * @return 更新日時
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 更新日時を設定します。
     *
     * @param updatedAt 更新日時
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
     * 完了状態かどうかを返します。
     *
     * @return 完了済みの場合は {@code true}
     */
    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    /**
     * 互換性維持用の完了状態 getter です。
     *
     * @return 完了済みの場合は {@code true}
     */
    public boolean getCompleted() {
        return isCompleted();
    }

    /**
     * 互換性維持用の完了状態 setter です。
     *
     * @param completed 完了状態
     */
    public void setCompleted(boolean completed) {
        this.status = completed ? Status.COMPLETED : Status.NOT_STARTED;
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
     * カテゴリを返します。
     *
     * @return カテゴリ
     */
    public Category getCategory() {
        return category;
    }

    /**
     * カテゴリを設定します。
     *
     * @param category カテゴリ
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * 所有ユーザーを返します。
     *
     * @return 所有ユーザー
     */
    public AppUser getUser() {
        return user;
    }

    /**
     * 所有ユーザーを設定します。
     *
     * @param user 所有ユーザー
     */
    public void setUser(AppUser user) {
        this.user = user;
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
     * カテゴリ色を返します。
     *
     * @return カテゴリ色
     */
    public String getCategoryColor() {
        return categoryColor;
    }

    /**
     * カテゴリ色を設定します。
     *
     * @param categoryColor カテゴリ色
     */
    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    /**
     * ユーザーIDを返します。
     *
     * @return ユーザーID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * ユーザーIDを設定します。
     *
     * @param userId ユーザーID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Todo{id=" + id + ", title='" + title + "', status=" + status + "}";
    }
}

