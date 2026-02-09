package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);

    private final TodoRepository todoRepository;
    private final ReminderNotificationService notificationService;
    private final String zoneId;

    public ReminderScheduler(TodoRepository todoRepository,
                             ReminderNotificationService notificationService,
                             @org.springframework.beans.factory.annotation.Value(
                                     "${app.mail.reminder-zone:Asia/Tokyo}") String zoneId) {
        this.todoRepository = todoRepository;
        this.notificationService = notificationService;
        this.zoneId = zoneId;
    }

    @Scheduled(cron = "${app.mail.reminder-cron:0 0 9 * * *}",
               zone = "${app.mail.reminder-zone:Asia/Tokyo}")
    @Transactional(readOnly = true)
    public void sendDailyReminders() {
        LocalDate today = LocalDate.now(ZoneId.of(zoneId));
        List<Todo> todos = todoRepository.findDueForReminder(today);
        if (todos.isEmpty()) {
            logger.info("No reminders to send for {}", today);
            return;
        }
        for (Todo todo : todos) {
            notificationService.sendReminder(todo);
        }
        logger.info("Reminder processing finished. total={}", todos.size());
    }
}
