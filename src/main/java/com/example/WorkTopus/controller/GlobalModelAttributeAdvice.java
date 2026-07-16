package com.example.WorkTopus.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Map;
import java.util.Optional;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalModelAttributeAdvice.class);

    private final UserRepository userRepository;
    // [추가] 오라클 DB에서 최신 워크스페이스/프로젝트 명을 긁어오기 위한 레포지토리 주입
    private final ManageRepository manageRepository;

    @ModelAttribute
    public void addLoginInfo(
            Model model,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (handler instanceof HandlerMethod handlerMethod) {
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();

            log.info("[{}] {} -> {}.{}()",
                    request.getMethod(),
                    request.getRequestURI(),
                    controllerName,
                    methodName
            );
        }

        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("loginUsername", loggedIn ? authentication.getName() : "");

        boolean isAdmin = loggedIn && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        model.addAttribute("isAdmin", isAdmin);


        // ID직접 조회
        if (loggedIn) {
            String username = authentication.getName();

            Optional<Users> dbUser = userRepository.findByUserId(username);

            if (dbUser.isPresent()) {
                model.addAttribute("user", dbUser.get());
            } else {
                model.addAttribute("user", null);
            }
        } else {
            model.addAttribute("user", null);
        }

        // ================= [여기서부터 프로젝트 공통 헤더 고정 핵심 로직 추가] =================
        // 현재 브라우저 주소창의 템플릿 변수({projectId} 혹은 {workspaceId})들을 전부 긁어옵니다.
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVariables != null) {
            String projectIdStr = pathVariables.get("projectId");
            String workspaceIdStr = pathVariables.get("workspaceId");
            String targetIdStr = (projectIdStr != null) ? projectIdStr : workspaceIdStr;

            if (targetIdStr != null) {
                try {
                    Long id = Long.parseLong(targetIdStr);
                    // 오라클 DB에서 실시간 최신 프로젝트명을 무조건 끄집어내어 모델에 강제 세팅합니다.
                    manageRepository.findById(id).ifPresent(manageData -> {
                        model.addAttribute("project", manageData);
                        model.addAttribute("projectId", id);
                    });
                } catch (NumberFormatException e) {
                    // 주소 뒤에 숫자가 아닌 글자가 오는 예외 케이스 처리
                }
            }
        }
        // ===============================================================================

        System.out.println("model:" + model.toString());
    }
}
