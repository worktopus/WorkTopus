package com.example.WorkTopus.controller;

import jakarta.servlet.http.HttpServletRequest;
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

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributeAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalModelAttributeAdvice.class);

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

        System.out.println("model:" + model.toString());
    }
}