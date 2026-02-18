package com.example.todo.controller;

import com.example.todo.entity.MailSettings;
import com.example.todo.service.MailService;
import com.example.todo.service.MailSettingsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/mail-settings")
public class AdminMailSettingsController {
    private final MailSettingsService mailSettingsService;
    private final MailService mailService;

    public AdminMailSettingsController(MailSettingsService mailSettingsService,
                                       MailService mailService) {
        this.mailSettingsService = mailSettingsService;
        this.mailService = mailService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String index(Model model) {
        MailSettings settings = mailSettingsService.getEffectiveSettings();
        model.addAttribute("settings", settings);
        return "admin/mail-settings";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveSettings(@RequestParam("username") String username,
                               @RequestParam("appPassword") String appPassword,
                               @RequestParam("fromAddress") String fromAddress,
                               RedirectAttributes redirectAttributes) {
        mailSettingsService.save(username, appPassword, fromAddress);
        redirectAttributes.addFlashAttribute("message", "メール設定を更新しました。");
        return "redirect:/admin/mail-settings";
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public String sendTestMail(@RequestParam("to") String to,
                               RedirectAttributes redirectAttributes) {
        String trimmed = to == null ? "" : to.trim();
        if (trimmed.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "送信先メールアドレスを入力してください。");
            return "redirect:/admin/mail-settings";
        }
        try {
            mailService.sendTextMail(trimmed, "Test Email", "This is a reminder email test.");
            redirectAttributes.addFlashAttribute("message", "テストメールを送信しました。");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "テストメールの送信に失敗しました。");
        }
        return "redirect:/admin/mail-settings";
    }
}
