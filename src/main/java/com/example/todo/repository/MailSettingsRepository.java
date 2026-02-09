package com.example.todo.repository;

import com.example.todo.entity.MailSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailSettingsRepository extends JpaRepository<MailSettings, Long> {
}
