package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.UserCreateForm;
import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.EmailVerificationService;
import com.example.WorkTopus.service.ProjectService;
import com.example.WorkTopus.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProjectService projectService;
    private final EmailVerificationService emailVerificationService;

    // 메인 페이지
    @GetMapping("/")
    public String index(Authentication authentication) {

        boolean loggedIn =
                authentication != null
                        && authentication.isAuthenticated()
                        && !(authentication instanceof AnonymousAuthenticationToken);

        if (loggedIn) {
            return "redirect:/projects";
        }

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
            @Valid
            @ModelAttribute("userForm")
            UserCreateForm userForm,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "home/register";
        }

        String submittedEmail =
                userForm.getEmail()
                        .trim()
                        .toLowerCase();

        String verifiedEmail =
                (String) session.getAttribute("verifiedEmail");

        if (!submittedEmail.equals(verifiedEmail)) {
            bindingResult.reject(
                    "emailVerificationRequired",
                    "이메일 인증을 완료해주세요."
            );

            return "home/register";
        }

        try {
            userService.register(userForm);

            emailVerificationService.removeVerificationCode(
                    submittedEmail
            );

            session.removeAttribute("verifiedEmail");

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
    // 아이디 중복확인
    @GetMapping("/home/check-id")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkUserId(
            @RequestParam String userId
    ) {
        try {
            boolean available =
                    userService.isUserIdAvailable(userId);

            return ResponseEntity.ok(
                    Map.of(
                            "available", available,
                            "message",
                            available
                                    ? "사용 가능한 아이디입니다."
                                    : "이미 사용 중인 아이디입니다."
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "available", false,
                            "message", e.getMessage()
                    )
            );
        }
    }

    // 이메일 인증
    // 이메일 인증번호 발송
    @PostMapping("/home/email/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEmailCode(
            @RequestParam String email,
            HttpSession session
    ) {
        try {
            emailVerificationService.sendVerificationCode(email);

            // 기존 이메일 인증 상태 초기화
            session.removeAttribute("verifiedEmail");

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "인증번호를 발송했습니다."
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message", "메일 발송에 실패했습니다."
                    )
            );
        }
    }

    // 이메일 인증번호 확인
    @PostMapping("/home/email/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyEmailCode(
            @RequestParam String email,
            @RequestParam String verificationCode,
            HttpSession session
    ) {
        try {
            emailVerificationService.verifyCode(
                    email,
                    verificationCode
            );

            String normalizedEmail =
                    email.trim().toLowerCase();

            // 실제 인증이 완료된 이메일을 세션에 저장
            session.setAttribute(
                    "verifiedEmail",
                    normalizedEmail
            );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "이메일 인증이 완료되었습니다."
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        }
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