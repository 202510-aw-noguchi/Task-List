package com.example.todo.controller;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.PasswordReissueService;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/password/reissue")
public class PasswordReissueController {
    private final UserRepository userRepository;
    private final PasswordReissueService passwordReissueService;

    public PasswordReissueController(UserRepository userRepository,
                                     PasswordReissueService passwordReissueService) {
        this.userRepository = userRepository;
        this.passwordReissueService = passwordReissueService;
    }

    @GetMapping
    public String showForm() {
        return "password-reissue";
    }

    @PostMapping
    public String submit(@RequestParam("username") String username,
                         @RequestParam("email") String email,
                         RedirectAttributes redirectAttributes) {
        String trimmedUsername = username == null ? "" : username.trim();
        String trimmedEmail = email == null ? "" : email.trim();
        if (trimmedUsername.isEmpty() || trimmedEmail.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ユーザー名とメールアドレスを入力してください。");
            return "redirect:/password/reissue";
        }

        Optional<AppUser> userOpt = userRepository.findByUsername(trimmedUsername);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ユーザー情報が一致しません。");
            return "redirect:/password/reissue";
        }

        AppUser user = userOpt.get();
        if (user.getEmail() == null || !trimmedEmail.equalsIgnoreCase(user.getEmail().trim())) {
            redirectAttributes.addFlashAttribute("error", "ユーザー情報が一致しません。");
            return "redirect:/password/reissue";
        }

        String oneTimePassword;
        try {
            oneTimePassword = passwordReissueService.issueAndSendOneTimePassword(user);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/password/reissue";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "再発行メールの送信に失敗しました。");
            return "redirect:/password/reissue";
        }

        String message = "ワンタイムパスワードをメール送信しました。受信後にログインしてください。";
        if (passwordReissueService.isMailDryRun()) {
            message += "（dry-run のため画面表示）OTP: " + oneTimePassword;
        }
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/login";
    }
}
