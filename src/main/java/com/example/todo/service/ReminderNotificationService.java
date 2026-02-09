package com.example.todo.service;

import com.example.todo.entity.Todo;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class ReminderNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(ReminderNotificationService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MailService mailService;
    private final TemplateEngine templateEngine;
    private final String subject;
    private final String appName;

    public ReminderNotificationService(MailService mailService,
                                       TemplateEngine templateEngine,
                                       @Value("${app.mail.reminder-subject:[Todo] 期限リマインダー}") String subject,
                                       @Value("${app.mail.app-name:Todo App}") String appName) {
        this.mailService = mailService;
        this.templateEngine = templateEngine;
        this.subject = subject;
        this.appName = appName;
    }

    @Async("emailExecutor")
    public void sendReminder(Todo todo) {
        if (todo == null || todo.getUser() == null || todo.getUser().getEmail() == null) {
            return;
        }
        String to = todo.getUser().getEmail();
        if (to.isBlank()) {
            return;
        }

        String text = buildText(todo);
        String html = buildHtml(todo);

        mailService.sendHtmlMail(to, subject, html, text);
        logger.info("Reminder mail queued for todoId={} to {}", todo.getId(), to);
    }

    private String buildText(Todo todo) {
        String deadline = todo.getDeadline() == null ? "未設定" : DATE_FORMAT.format(todo.getDeadline());
        StringBuilder builder = new StringBuilder();
        builder.append(appName).append(" 期限リマインダー").append(System.lineSeparator());
        builder.append("タイトル: ").append(Objects.toString(todo.getTitle(), "")).append(System.lineSeparator());
        builder.append("期限: ").append(deadline).append(System.lineSeparator());
        builder.append("詳細: ").append(Objects.toString(todo.getDetail(), "")).append(System.lineSeparator());
        builder.append("優先度: ").append(todo.getPriority() == null ? "" : todo.getPriority().name());
        return builder.toString();
    }

    private String buildHtml(Todo todo) {
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("title", Objects.toString(todo.getTitle(), ""));
        context.setVariable("detail", Objects.toString(todo.getDetail(), ""));
        context.setVariable("deadline",
                todo.getDeadline() == null ? "未設定" : DATE_FORMAT.format(todo.getDeadline()));
        context.setVariable("priority", todo.getPriority() == null ? "" : todo.getPriority().name());
        return templateEngine.process("mail/reminder", context);
    }
}
