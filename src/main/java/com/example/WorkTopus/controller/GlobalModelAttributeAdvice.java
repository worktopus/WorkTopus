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
import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.repository.ManageMemberRepository;
import com.example.WorkTopus.manage.entity.ManageMember;

// 자바 표준 컬렉션 및 스트림 유틸 임포트 라인
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

    // 오라클 DB에서 최신 워크스페이스/프로젝트 명을 긁어오기 위한 레포지토리 주입
    private final ManageRepository manageRepository;

    // 현재 로그인한 사용자가 프로젝트 참여자인지 확인하기 위한 레포지토리
    private final ProjectMemberRepository projectMemberRepository;

    // 전역 헤더 프로필의 'assignedRole'을 실시간 영구 조회 및 매핑하기 위한 레포지토리 인젝션
    private final ManageMemberRepository manageMemberRepository;

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

        /*
         * 로그인 사용자 정보를 프로젝트 참여자 검사에서도
         * 사용하기 위해 if문 바깥에 선언합니다.
         */
        Optional<Users> dbUser =
                Optional.empty();


        // ID직접 조회
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

        // 현재 브라우저 주소창의 템플릿 변수({projectId} 혹은 {workspaceId})들을 전부 긁어옵니다.
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                (Map<String, String>)
                        request.getAttribute(
                                HandlerMapping
                                        .URI_TEMPLATE_VARIABLES_ATTRIBUTE
                        );

        if (pathVariables != null) {
            String projectIdStr =
                    pathVariables.get("projectId");

            String workspaceIdStr =
                    pathVariables.get("workspaceId");

            String targetIdStr =
                    projectIdStr != null
                            ? projectIdStr
                            : workspaceIdStr;

            if (targetIdStr != null) {
                try {
                    Long id =
                            Long.parseLong(targetIdStr);


                    /*
                     * 현재 로그인한 사용자의 DB 정보를 가져옵니다.
                     *
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

                    // 2. 📌 [교정 완료] ManageMember의 연관관계 실제 명칭인 getUser()로 정밀 매핑하여 필터링합니다.
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
        }

        // ===============================================================================

        System.out.println(
                "model:" + model.toString()
        );
    }
}
