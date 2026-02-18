package com.example.todo.service;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordReissueService {
    private static final String OTP_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int OTP_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final String appBaseUrl;
    private final boolean mailDryRun;

    public PasswordReissueService(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  MailService mailService,
                                  @Value("${app.base-url:http://localhost:8080}") String appBaseUrl,
                                  @Value("${app.mail.dry-run:false}") boolean mailDryRun) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.appBaseUrl = trimTrailingSlash(appBaseUrl);
        this.mailDryRun = mailDryRun;
    }

    public String issueAndSendOneTimePassword(AppUser user) {
        String to = user.getEmail() == null ? "" : user.getEmail().trim();
        if (to.isEmpty()) {
            throw new IllegalArgumentException("メールアドレス未設定のため再発行できません。");
        }

        String previousPassword = user.getPassword();
        boolean previousRequired = user.isPasswordResetRequired();
        LocalDateTime previousIssuedAt = user.getPasswordResetIssuedAt();

        String oneTimePassword = generateOneTimePassword();
        user.setPassword(passwordEncoder.encode(oneTimePassword));
        user.setPasswordResetRequired(true);
        user.setPasswordResetIssuedAt(LocalDateTime.now());
        userRepository.save(user);

        String loginUrl = appBaseUrl + "/login";
        String resetUrl = appBaseUrl + "/password/reset";
        String subject = "[Todo] ワンタイムパスワード再発行のお知らせ";
        String body = """
                ToDoシステムのワンタイムパスワードを発行しました。

                ユーザーID: %s
                ワンタイムパスワード: %s

                ログインURL: %s
                ※ログイン後はパスワード再設定画面へ自動遷移します。
                参考URL: %s
                """.formatted(user.getUsername(), oneTimePassword, loginUrl, resetUrl);

        try {
            mailService.sendTextMail(to, subject, body);
            return oneTimePassword;
        } catch (Exception ex) {
            user.setPassword(previousPassword);
            user.setPasswordResetRequired(previousRequired);
            user.setPasswordResetIssuedAt(previousIssuedAt);
            userRepository.save(user);
            throw new IllegalStateException("ワンタイムパスワードのメール送信に失敗しました。", ex);
        }
    }

    public boolean isMailDryRun() {
        return mailDryRun;
    }

    private String generateOneTimePassword() {
        StringBuilder builder = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(OTP_CHARS.length());
            builder.append(OTP_CHARS.charAt(index));
        }
        return builder.toString();
    }

    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8080";
        }
        String value = baseUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
