package com.example.WorkTopus.controller;

import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.ProjectService;
import com.example.WorkTopus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProjectService projectService;

    @GetMapping("/")
    public String index() {
        return "home/index";
    }

    @GetMapping("/login")
    public String login() {
        return "home/login";
    }

    @GetMapping("/projects")
    public String projects(Authentication authentication, Model model) {
        Users loginUser = userService.findByUserId(authentication.getName());
        List<Projects> projects = projectService.findProjectsByOwner(loginUser);

        model.addAttribute("projects", projects);

        return "workspace/projects";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}