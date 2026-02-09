package com.example.todo.service;

import com.example.todo.entity.MailSettings;
import com.example.todo.repository.MailSettingsRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailSettingsService {
    private static final Long SETTINGS_ID = 1L;

    private final MailSettingsRepository mailSettingsRepository;
    private final MailProperties mailProperties;

    public MailSettingsService(MailSettingsRepository mailSettingsRepository,
                               MailProperties mailProperties) {
        this.mailSettingsRepository = mailSettingsRepository;
        this.mailProperties = mailProperties;
    }

    @Transactional(readOnly = true)
    public MailSettings getStoredSettings() {
        return mailSettingsRepository.findById(SETTINGS_ID).orElse(null);
    }

    @Transactional(readOnly = true)
    public MailSettings getEffectiveSettings() {
        MailSettings stored = getStoredSettings();
        MailSettings effective = new MailSettings();
        effective.setId(SETTINGS_ID);
        effective.setUsername(firstNonBlank(
                stored == null ? null : stored.getUsername(),
                mailProperties.getUsername()));
        effective.setAppPassword(firstNonBlank(
                stored == null ? null : stored.getAppPassword(),
                mailProperties.getPassword()));
        effective.setFromAddress(firstNonBlank(
                stored == null ? null : stored.getFromAddress(),
                mailProperties.getUsername()));
        effective.setUpdatedAt(stored == null ? null : stored.getUpdatedAt());
        return effective;
    }

    @Transactional
    public MailSettings save(String username, String appPassword, String fromAddress) {
        MailSettings settings = mailSettingsRepository.findById(SETTINGS_ID).orElseGet(() -> {
            MailSettings created = new MailSettings();
            created.setId(SETTINGS_ID);
            return created;
        });
        if (username != null && !username.isBlank()) {
            settings.setUsername(username.trim());
        }
        if (appPassword != null && !appPassword.isBlank()) {
            settings.setAppPassword(appPassword);
        }
        if (fromAddress != null && !fromAddress.isBlank()) {
            settings.setFromAddress(fromAddress.trim());
        }
        settings.setUpdatedAt(LocalDateTime.now());
        return mailSettingsRepository.save(settings);
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return Objects.toString(fallback, "");
    }
}
