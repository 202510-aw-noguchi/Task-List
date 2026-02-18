package com.example.todo.controller;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.PasswordReissueService;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordReissueService passwordReissueService;

    public AdminUserController(UserRepository userRepository,
                               TodoRepository todoRepository,
                               PasswordEncoder passwordEncoder,
                               PasswordReissueService passwordReissueService) {
        this.userRepository = userRepository;
        this.todoRepository = todoRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordReissueService = passwordReissueService;
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
        String oneTimePassword;
        try {
            oneTimePassword = passwordReissueService.issueAndSendOneTimePassword(user);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/users/manage";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "ワンタイムパスワードのメール送信に失敗しました。");
            return "redirect:/admin/users/manage";
        }

        String message = "ユーザー " + user.getUsername() + " のワンタイムパスワードを再発行し、メール送信しました。";
        if (passwordReissueService.isMailDryRun()) {
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
}
