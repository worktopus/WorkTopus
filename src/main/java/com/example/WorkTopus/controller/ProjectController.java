package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.ProjectCreateForm;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.ProjectService;
import com.example.WorkTopus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    // 프로젝트 생성 화면
    @GetMapping("/projects/new")
    public String projectCreateForm(Model model) {
        model.addAttribute("projectCreateForm", new ProjectCreateForm());
        return "workspace/project-create";
    }

    // 프로젝트 생성
    @PostMapping("/projects")
    public String createProject(
            @Valid @ModelAttribute("projectCreateForm")
            ProjectCreateForm form,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "workspace/project-create";
        }

        Users loginUser =
                userService.findByUserId(authentication.getName());

        projectService.createProject(form, loginUser);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "프로젝트가 생성되었습니다."
        );

        return "redirect:/projects";
    }

    // 초대 코드로 프로젝트 참여
    @PostMapping("/projects/join")
    public String joinProject(
            @RequestParam("inviteCode") String inviteCode,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Users loginUser =
                userService.findByUserId(authentication.getName());

        try {
            projectService.joinProject(inviteCode, loginUser);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "프로젝트에 참여했습니다."
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/projects";
    }
}