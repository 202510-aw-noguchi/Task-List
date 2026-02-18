package com.example.todo.controller;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.MailService;
import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users/manage")
public class AdminUserController {
    private static final String OTP_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int OTP_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final String appBaseUrl;
    private final boolean mailDryRun;

    public AdminUserController(UserRepository userRepository,
                               TodoRepository todoRepository,
                               PasswordEncoder passwordEncoder,
                               MailService mailService,
                               @Value("${app.base-url:http://localhost:8080}") String appBaseUrl,
                               @Value("${app.mail.dry-run:false}") boolean mailDryRun) {
        this.userRepository = userRepository;
        this.todoRepository = todoRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.appBaseUrl = trimTrailingSlash(appBaseUrl);
        this.mailDryRun = mailDryRun;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String manage(Model model) {
        List<AppUser> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users-manage";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         @RequestParam("role") String role,
                         @RequestParam(value = "email", required = false) String email,
                         RedirectAttributes redirectAttributes) {
        String trimmedUsername = username == null ? "" : username.trim();
        if (trimmedUsername.isEmpty() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "ユーザー名とパスワードは必須です。");
            return "redirect:/admin/users/manage";
        }

        if (userRepository.findByUsername(trimmedUsername).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "同じユーザー名が既に存在します。");
            return "redirect:/admin/users/manage";
        }

        AppUser user = new AppUser();
        user.setUsername(trimmedUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(normalizeRole(role));
        String trimmedEmail = email == null ? "" : email.trim();
        user.setEmail(trimmedEmail.isEmpty() ? null : trimmedEmail);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "ユーザーを追加しました。");
        return "redirect:/admin/users/manage";
    }

    @PostMapping("/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRole(@RequestParam("userId") Long userId,
                             @RequestParam("role") String role,
                             @RequestParam(value = "email", required = false) String email,
                             RedirectAttributes redirectAttributes) {
        Optional<AppUser> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ユーザーが見つかりません。");
            return "redirect:/admin/users/manage";
        }

        AppUser user = userOpt.get();
        user.setRole(normalizeRole(role));
        String trimmedEmail = email == null ? "" : email.trim();
        user.setEmail(trimmedEmail.isEmpty() ? null : trimmedEmail);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "権限を更新しました。");
        return "redirect:/admin/users/manage";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@RequestParam("userId") Long userId,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        Optional<AppUser> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ユーザーが見つかりません。");
            return "redirect:/admin/users/manage";
        }

        AppUser user = userOpt.get();
        if (principal != null && user.getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "ログイン中のアカウントは削除できません。");
            return "redirect:/admin/users/manage";
        }

        if (todoRepository.existsByUser_Id(userId)) {
            redirectAttributes.addFlashAttribute("error", "タスクが存在するため削除できません。");
            return "redirect:/admin/users/manage";
        }

        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("message", "ユーザーを削除しました。");
        return "redirect:/admin/users/manage";
    }

    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String reissuePassword(@RequestParam("userId") Long userId,
                                  RedirectAttributes redirectAttributes) {
        Optional<AppUser> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ユーザーが見つかりません。");
            return "redirect:/admin/users/manage";
        }

        AppUser user = userOpt.get();
        String to = user.getEmail() == null ? "" : user.getEmail().trim();
        if (to.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "メールアドレス未設定のため再発行できません。");
            return "redirect:/admin/users/manage";
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
        } catch (Exception ex) {
            user.setPassword(previousPassword);
            user.setPasswordResetRequired(previousRequired);
            user.setPasswordResetIssuedAt(previousIssuedAt);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("error", "ワンタイムパスワードのメール送信に失敗しました。");
            return "redirect:/admin/users/manage";
        }

        String message = "ユーザー " + user.getUsername() + " のワンタイムパスワードを再発行し、メール送信しました。";
        if (mailDryRun) {
            message += "（dry-run のため画面表示）OTP: " + oneTimePassword;
        }
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/admin/users/manage";
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "ROLE_USER";
        }
        String upper = role.trim().toUpperCase(Locale.ROOT);
        if (upper.equals("ADMIN") || upper.equals("ROLE_ADMIN")) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
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
