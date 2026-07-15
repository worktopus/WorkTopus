package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatUserController {

    private final UserService userService;

    @GetMapping("/api/chat/me")
    public Map<String, Object> getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        Users user = userService.findByUserId(principal.getName());

        return Map.of(
                "userNum", user.getUserNum(),
                "userId", user.getUserId(),
                "name", user.getName()
        );
    }
}
