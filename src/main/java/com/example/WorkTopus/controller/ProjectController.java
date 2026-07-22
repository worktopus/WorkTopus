package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.ProjectCreateForm;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.repository.ManageRepository;
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
    // [추가] 오라클 DB에서 최신 워크스페이스/프로젝트 정보를 조회하기 위한 레포지토리 주입
    private final ManageRepository manageRepository;

    /**
     * [핵심 추가] 공통 데이터 매핑 로직
     * 이 컨트롤러 하위의 화면을 호출할 때 URL 주소창에 {projectId} 정보가 있다면
     * 오라클 DB에서 실시간으로 최신 이름을 조회하여 헤더용 모델("project")에 자동으로 전달합니다.
     */
    @ModelAttribute
    public void addGlobalProjectHeader(
            @PathVariable(value = "projectId", required = false) Long projectId,
            Model model
    ) {
        if (projectId != null) {
            manageRepository.findById(projectId).ifPresent(manageData -> {
                // 공통 header.html 템플릿이 사용하는 "project" 객체 명칭으로 실시간 주입
                model.addAttribute("project", manageData);
                // 사이드바 링크 생성용 ID 값도 누락되지 않도록 매핑
                model.addAttribute("projectId", projectId);
            });
        }
    }

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
