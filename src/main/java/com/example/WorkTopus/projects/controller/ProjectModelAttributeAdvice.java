package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.ProjectRepository;
import com.example.WorkTopus.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/**
 * 프로젝트 관련 화면에서 공통으로 사용하는 Model 데이터를 설정하는 ControllerAdvice.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ProjectModelAttributeAdvice {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @ModelAttribute
    public void addProjectInfo(
            Model model,
            Authentication authentication,
            HttpServletRequest request
    ) {
        // 기본값은 팀장이 아닌 것으로 설정
        model.addAttribute("isProjectOwner", false);

        // 로그인하지 않은 사용자는 처리하지 않음
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        // URL 경로에서 projectId 추출
        Object attribute =
                request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (!(attribute instanceof Map<?, ?> variables)) {
            return;
        }

        Object projectIdValue = variables.get("projectId");

        if (projectIdValue == null) {
            return;
        }

        Long projectId = Long.valueOf(projectIdValue.toString());

        // 현재 로그인한 사용자 조회
        Users loginUser = userRepository
                .findByUserId(authentication.getName())
                .orElse(null);

        if (loginUser == null) {
            return;
        }

        // 프로젝트 소유자 여부 확인
        boolean isProjectOwner = projectRepository.findById(projectId)
                .map(project ->
                        project.getOwner() != null
                                && project.getOwner().getUserNum()
                                .equals(loginUser.getUserNum())
                )
                .orElse(false);

        // View에서 사용할 팀장 여부 전달
        model.addAttribute("isProjectOwner", isProjectOwner);
    }
}