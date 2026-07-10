package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.UserCreateForm;
import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.ProjectService;
import com.example.WorkTopus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProjectService projectService;

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "home/index";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String login() {
        return "home/login";
    }

    // 회원가입 페이지
    @GetMapping("/home/register")
    public String registerForm(Model model) {
        model.addAttribute("userForm", new UserCreateForm());

        return "home/register";
    }

    // 회원가입 처리
    @PostMapping("/home/register")
    public String registerUser(
            @Valid @ModelAttribute("userForm") UserCreateForm userForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "home/register";
        }

        try {
            userService.register(userForm);

        } catch (IllegalArgumentException e) {
            bindingResult.reject(
                    "registerFail",
                    e.getMessage()
            );

            return "home/register";
        }

        redirectAttributes.addFlashAttribute(
                "msg",
                "회원가입이 완료되었습니다. 로그인하세요."
        );

        return "redirect:/login";
    }

    // 로그인한 사용자의 프로젝트 목록
    @GetMapping("/projects")
    public String projects(
            Authentication authentication,
            Model model
    ) {
        Users loginUser =
                userService.findByUserId(authentication.getName());

        List<Projects> projects =
                projectService.findProjectsByUser(loginUser);

        model.addAttribute("projects", projects);

        return "workspace/projects";
    }

    // 접근 권한 없음
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}