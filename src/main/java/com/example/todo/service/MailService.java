package com.example.todo.service;

import com.example.todo.entity.MailSettings;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final MailSettingsService mailSettingsService;
    private final MailProperties mailProperties;
    private final boolean dryRun;

    public MailService(JavaMailSender mailSender,
                       MailSettingsService mailSettingsService,
                       MailProperties mailProperties,
                       @org.springframework.beans.factory.annotation.Value("${app.mail.dry-run:false}") boolean dryRun) {
        this.mailSender = mailSender;
        this.mailSettingsService = mailSettingsService;
        this.mailProperties = mailProperties;
        this.dryRun = dryRun;
        logger.info("MailService initialized. dryRun={}", dryRun);
    }

    public void sendTextMail(String to, String subject, String text) {
        MailSettings effective = mailSettingsService.getEffectiveSettings();
        if (dryRun) {
            logger.info("DRY RUN mail(text) to={} subject={} body={}", to, subject, text);
            return;
        }
        JavaMailSender sender = buildSender(effective);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(effective.getFromAddress());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        sender.send(message);
        logger.info("Text mail sent to {}", to);
    }

    public void sendHtmlMail(String to, String subject, String html, String textAlternative) {
        MailSettings effective = mailSettingsService.getEffectiveSettings();
        if (dryRun) {
            logger.info("DRY RUN mail(html) to={} subject={} text={} html={}", to, subject, textAlternative, html);
            return;
        }
        JavaMailSender sender = buildSender(effective);
        MimeMessage mimeMessage = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(effective.getFromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textAlternative, html);
            sender.send(mimeMessage);
            logger.info("HTML mail sent to {}", to);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Failed to send HTML mail", ex);
        }
    }

    private JavaMailSender buildSender(MailSettings settings) {
        if (settings == null || settings.getUsername() == null || settings.getUsername().isBlank()) {
            return mailSender;
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailProperties.getHost());
        sender.setPort(mailProperties.getPort());
        sender.setProtocol(mailProperties.getProtocol());
        sender.setUsername(settings.getUsername());
        sender.setPassword(settings.getAppPassword());
        if (mailProperties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
        }
        Properties props = new Properties();
        props.putAll(mailProperties.getProperties());
        sender.setJavaMailProperties(props);
        return sender;
    }
}
