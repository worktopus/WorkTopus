package com.example.WorkTopus.controller;

import com.example.WorkTopus.repository.ProjectMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.UserRepository;
import com.example.WorkTopus.service.ProjectService;
import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.repository.ManageMemberRepository;
import com.example.WorkTopus.manage.entity.ManageMember;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalModelAttributeAdvice.class);

    private final UserRepository userRepository;
    private final ManageRepository manageRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ManageMemberRepository manageMemberRepository;
    private final ProjectService projectService;

    @ModelAttribute
    public void addLoginInfo(
            Model model,
            Authentication authentication,
            HttpServletRequest request
    ) {
        // 📌 API 요청(/api/...)은 전역 모델 주입 및 프로젝트 권한 검사 대상에서 완전히 제외합니다.
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) {
            return;
        }

        Object handler =
                request.getAttribute(
                        HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE
                );

        if (handler instanceof HandlerMethod handlerMethod) {
            String controllerName =
                    handlerMethod.getBeanType().getSimpleName();

            String methodName =
                    handlerMethod.getMethod().getName();

            log.info(
                    "[{}] {} -> {}.{}()",
                    request.getMethod(),
                    request.getRequestURI(),
                    controllerName,
                    methodName
            );
        }

        boolean loggedIn =
                authentication != null
                        && authentication.isAuthenticated()
                        && !(authentication
                        instanceof AnonymousAuthenticationToken);

        model.addAttribute("loggedIn", loggedIn);

        model.addAttribute(
                "loginUsername",
                loggedIn ? authentication.getName() : ""
        );

        boolean isAdmin =
                loggedIn
                        && authentication
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch("ROLE_ADMIN"::equals);

        model.addAttribute("isAdmin", isAdmin);

        Optional<Users> dbUser = Optional.empty();

        if (loggedIn) {
            String username = authentication.getName();
            dbUser = userRepository.findByUserId(username);

            if (dbUser.isPresent()) {
                model.addAttribute("user", dbUser.get());
            } else {
                model.addAttribute("user", null);
            }
        } else {
            model.addAttribute("user", null);
        }

        // ================= [프로젝트 공통 헤더 로직] =================

        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                (Map<String, String>)
                        request.getAttribute(
                                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
                        );

        String targetIdStr = null;

        // 📌 중요: 단순 {id}는 알림 ID나 게시글 ID일 수 있으므로 제외하고, 명확한 projectId/workspaceId만 타겟팅합니다.
        if (pathVariables != null) {
            String projectIdStr = pathVariables.get("projectId");
            String workspaceIdStr = pathVariables.get("workspaceId");

            targetIdStr = projectIdStr != null ? projectIdStr : workspaceIdStr;
        }

        // URL 경로 세그먼트 정밀 추출
        if (targetIdStr == null && uri != null) {
            String[] segments = uri.split("/");
            for (int i = 0; i < segments.length; i++) {
                if (("projects".equals(segments[i]) || "manage".equals(segments[i])) && (i + 1 < segments.length)) {
                    String nextSegment = segments[i + 1];
                    if (nextSegment.matches("\\d+")) {
                        targetIdStr = nextSegment;
                        break;
                    }
                }
            }
        }

        // 추출에 성공했다면 권한 체크 실행
        if (targetIdStr != null) {
            try {
                Long id = Long.parseLong(targetIdStr);

                Users loginUser =
                        dbUser.orElseThrow(
                                () ->
                                        new AccessDeniedException(
                                                "로그인 사용자 정보를 확인할 수 없습니다."
                                        )
                        );

                boolean isProjectMember =
                        projectMemberRepository
                                .existsByProject_IdAndUser_UserNum(
                                        id,
                                        loginUser.getUserNum()
                                );

                if (!isProjectMember) {
                    log.warn(
                            "프로젝트 접근 거부 - projectId: {}, userNum: {}",
                            id,
                            loginUser.getUserNum()
                    );

                    throw new AccessDeniedException(
                            "해당 프로젝트에 접근할 권한이 없습니다."
                    );
                }

                manageRepository
                        .findById(id)
                        .ifPresent(manageData -> {
                            model.addAttribute("project", manageData);
                            model.addAttribute("projectId", id);
                        });

                List<ManageMember> globalMembersList = manageMemberRepository.findByWorkspaceId(id);

                ManageMember myHeaderMemberInfo = globalMembersList.stream()
                        .filter(m -> m.getUser() != null && loginUser.getUserId().equals(m.getUser().getUserId()))
                        .findFirst()
                        .orElse(null);

                model.addAttribute("projectMember", myHeaderMemberInfo);

            } catch (NumberFormatException e) {
                throw new AccessDeniedException("올바르지 않은 프로젝트 번호입니다.");
            }
        }

        // 상시 프로젝트 드롭다운 목록
        if (loggedIn && dbUser.isPresent()) {
            try {
                Users loginUser = dbUser.get();
                List projects = projectService.findProjectsByUser(loginUser);
                model.addAttribute("projects", projects);
            } catch (Exception e) {
                log.error("[오라클 DB 하위 경로 주입 에러] 원인: ", e);
                model.addAttribute("projects", List.of());
            }
        } else {
            model.addAttribute("projects", List.of());
        }
    }

    // 삭제되거나 없는 게시글 접근 시 /access-denied 페이지로 직접 리다이렉트
    @org.springframework.web.bind.annotation.ExceptionHandler(com.example.WorkTopus.projects.exception.BoardNotFoundException.class)
    public String handleBoardNotFoundException(com.example.WorkTopus.projects.exception.BoardNotFoundException e) {
        log.warn("존재하지 않거나 삭제된 게시글 접근: {}", e.getMessage());
        return "redirect:/access-denied";
    }

}