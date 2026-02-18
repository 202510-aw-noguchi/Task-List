package com.example.todo.controller;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserRepository;
import java.security.Principal;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/password/reset")
public class PasswordResetController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showResetPage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Optional<AppUser> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login?error";
        }
        model.addAttribute("username", userOpt.get().getUsername());
        return "password-reset";
    }

    @PostMapping
    public String resetPassword(Principal principal,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<AppUser> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login?error";
        }

        if (newPassword == null || confirmPassword == null || newPassword.isBlank() || confirmPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "新しいパスワードと確認用パスワードは必須です。");
            return "redirect:/password/reset";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "新しいパスワードと確認用パスワードが一致しません。");
            return "redirect:/password/reset";
        }

        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "パスワードは8文字以上で入力してください。");
            return "redirect:/password/reset";
        }

        AppUser user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequired(false);
        user.setPasswordResetIssuedAt(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "パスワードを更新しました。");
        return "redirect:/";
    }
}
