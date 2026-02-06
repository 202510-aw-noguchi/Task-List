package com.example.todo.controller;

import com.example.todo.entity.Category;
import com.example.todo.repository.CategoryRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/groups")
public class AdminGroupController {
    private final CategoryRepository categoryRepository;

    public AdminGroupController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String groups(Model model) {
        List<Category> groups = categoryRepository.findAll();
        model.addAttribute("groups", groups);
        return "admin/groups";
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@RequestParam("groupId") Long groupId,
                         @RequestParam("name") String name,
                         @RequestParam("color") String color,
                         RedirectAttributes redirectAttributes) {
        if (groupId == null) {
            redirectAttributes.addFlashAttribute("error", "グループが選択されていません。");
            return "redirect:/admin/groups";
        }
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "グループ名を入力してください。");
            return "redirect:/admin/groups";
        }
        Category group = categoryRepository.findById(groupId).orElse(null);
        if (group == null) {
            redirectAttributes.addFlashAttribute("error", "対象のグループが見つかりません。");
            return "redirect:/admin/groups";
        }
        group.setName(trimmed);
        if (color != null && !color.isBlank()) {
            group.setColor(color.trim());
        }
        categoryRepository.save(group);
        redirectAttributes.addFlashAttribute("message", "グループを更新しました。");
        return "redirect:/admin/groups";
    }
}
