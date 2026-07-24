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
import java.util.stream.Collectors;

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

        model.addAttribute(
                "loggedIn",
                loggedIn
        );

        model.addAttribute(
                "loginUsername",
                loggedIn
                        ? authentication.getName()
                        : ""
        );

        boolean isAdmin =
                loggedIn
                        && authentication
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch("ROLE_ADMIN"::equals);

        model.addAttribute(
                "isAdmin",
                isAdmin
        );

        Optional<Users> dbUser =
                Optional.empty();

        if (loggedIn) {
            String username =
                    authentication.getName();

            dbUser =
                    userRepository.findByUserId(username);

            if (dbUser.isPresent()) {
                model.addAttribute(
                        "user",
                        dbUser.get()
                );
            } else {
                model.addAttribute(
                        "user",
                        null
                );
            }

        } else {
            model.addAttribute(
                    "user",
                    null
            );
        }
        // ================= [여기서부터 프로젝트 공통 헤더 고정 핵심 로직 추가] =================

        // 1. 현재 브라우저 주소창의 템플릿 변수들을 전부 긁어옵니다.
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                (Map<String, String>)
                        request.getAttribute(
                                HandlerMapping
                                        .URI_TEMPLATE_VARIABLES_ATTRIBUTE
                        );

        String targetIdStr = null;

        // 기존의 변수 매핑 방식 시도
        if (pathVariables != null) {
            String projectIdStr = pathVariables.get("projectId");
            String workspaceIdStr = pathVariables.get("workspaceId");
            String idStr = pathVariables.get("id"); // 일반적인 {id} 패턴 방어용

            targetIdStr = projectIdStr != null ? projectIdStr : (workspaceIdStr != null ? workspaceIdStr : idStr);
        }

        // 📌 [교정 핵심 원인 차단] 만약 하위 메뉴 진입으로 인해 pathVariables가 변수명을 놓쳤을 경우,
        // 실제 유저님이 요청하신 물리적인 브라우저 URL 경로 문자열에서 직접 프로젝트 ID 숫자를 정밀 정제해냅니다.
        if (targetIdStr == null) {
            String uri = request.getRequestURI(); // 예: /projects/24/boards/dashboard 또는 /projects/manage/24
            if (uri != null) {
                String[] segments = uri.split("/");
                for (int i = 0; i < segments.length; i++) {
                    // /projects/24 형태이거나 /manage/24 형태일 때 다음 세그먼트의 숫자를 확보
                    if (("projects".equals(segments[i]) || "manage".equals(segments[i])) && (i + 1 < segments.length)) {
                        String nextSegment = segments[i + 1];
                        if (nextSegment.matches("\\d+")) { // 순수 숫자로만 구성되어 있는지 정규식 검증
                            targetIdStr = nextSegment;
                            break;
                        }
                    }
                }
            }
        }

        // 추출에 성공했다면, 권한 체크 및 실시간 이름/역할(assignedRole) 주입 프로세스를 개시합니다.
        if (targetIdStr != null) {
            try {
                Long id = Long.parseLong(targetIdStr);

                /*
                 * 현재 로그인한 사용자의 DB 정보를 가져옵니다.
                 * 로그인은 되어 있지만 USERS 테이블에서
                 * 사용자 정보를 찾을 수 없다면 접근을 거부합니다.
                 */
                Users loginUser =
                        dbUser.orElseThrow(
                                () ->
                                        new AccessDeniedException(
                                                "로그인 사용자 정보를 확인할 수 없습니다."
                                        )
                        );

                /*
                 * 현재 로그인한 사용자가 해당 프로젝트의
                 * 참여자로 등록되어 있는지 확인합니다.
                 *
                 * 검사 기준:
                 * - URL에서 가져온 프로젝트 번호
                 * - 로그인 사용자의 USER_NUM
                 */
                boolean isProjectMember =
                        projectMemberRepository
                                .existsByProject_IdAndUser_UserNum(
                                        id,
                                        loginUser.getUserNum()
                                );

                /*
                 * 프로젝트 참여자가 아니면
                 * 프로젝트 내부 화면 접근을 차단합니다.
                 */
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

                /*
                 * 프로젝트 참여자 검사를 통과한 경우에만
                 * 오라클 DB에서 최신 프로젝트 정보를 조회합니다.
                 */
                manageRepository
                        .findById(id)
                        .ifPresent(manageData -> {
                            model.addAttribute(
                                    "project",
                                    manageData
                            );

                            model.addAttribute(
                                    "projectId",
                                    id
                            );
                        });

                // =========================================================================
                // 📌 [전역 고정 해결 완결본] 모든 사이드바 메뉴 진입 시 내 담당 역할을 실시간 주입합니다.
                // =========================================================================
                // 1. 현재 프로젝트 번호(id)를 기준으로 참여 멤버 목록을 조회합니다.
                List<ManageMember> globalMembersList = manageMemberRepository.findByWorkspaceId(id);

                // 2. 📌 ManageMember의 연관관계 실제 명칭인 getUser()로 정밀 매핑하여 필터링합니다.
                ManageMember myHeaderMemberInfo = globalMembersList.stream()
                        .filter(m -> m.getUser() != null && loginUser.getUserId().equals(m.getUser().getUserId()))
                        .findFirst()
                        .orElse(null);

                // 3. 공통 fragments/header.html 템플릿이 수집해가는 "projectMember" 명칭으로 최신 데이터 전역 사출
                model.addAttribute("projectMember", myHeaderMemberInfo);

            } catch (NumberFormatException e) {
                // 주소 뒤에 숫자가 아닌 글자가 오는 예외 케이스 처리
                throw new AccessDeniedException(
                        "올바르지 않은 프로젝트 번호입니다."
                );
            }
        }

        // ===============================================================================
        // 📌 어떤 주소창(if문 내부 환경)이든 무관하게 상시 프로젝트 드롭다운 목록을 전체 노출
        // ===============================================================================
        if (loggedIn && dbUser.isPresent()) {
            try {
                Users loginUser = dbUser.get();

                // 어떤 사이드바 메뉴 탭(대시보드, 칸반보드, 캘린더, 설정 등)을 눌러 이동해도
                // 오라클 DB에서 유저가 속한 전체 프로젝트 리스트를 완벽하게 유지하여 전달합니다.
                List projects = projectService.findProjectsByUser(loginUser);

                model.addAttribute("projects", projects);

                log.info("[오라클 DB 하위 경로 매핑 완결] 유저 ID: {}, 로드된 프로젝트 수: {}개", loginUser.getUserId(), projects.size());
            } catch (Exception e) {
                log.error("[오라클 DB 하위 경로 주입 에러] 원인: ", e);
                model.addAttribute("projects", List.of());
            }
        } else {
            model.addAttribute("projects", List.of());
        }

        System.out.println(
                "model:" + model.toString()
        );
    }
}
