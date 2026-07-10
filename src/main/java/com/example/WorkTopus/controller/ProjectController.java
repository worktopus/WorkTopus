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

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @GetMapping("/projects/new")
    public String projectCreateForm(Model model) {
        model.addAttribute("projectCreateForm", new ProjectCreateForm());
        return "workspace/project-create";
    }

    @PostMapping("/projects")
    public String createProject(
            @Valid @ModelAttribute("projectCreateForm") ProjectCreateForm form,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        if (bindingResult.hasErrors()) {
            return "workspace/project-create";
        }

        Users loginUser = userService.findByUserId(authentication.getName());

        projectService.createProject(form, loginUser);

        return "redirect:/projects";
    }
}