package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import com.example.WorkTopus.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectApiController {

    private final UserService userService;
    private final ProjectRepository projectRepository;

    /**
     * 헤더 드롭다운 전용 비동기 목록 조회 API
     * 기존 HomeController에서 사용하던 안정적인 원본 메서드를 그대로 활용합니다.
     */
    @GetMapping("/api/projects/my-list")
    public List<Projects> getMyProjectList(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.Collections.emptyList();
        }

        // 1. 로그인 유저 정보 조회
        Users loginUser = userService.findByUserId(authentication.getName());

        // 2. 검증된 기존 원본 레포지토리 메서드로 프로젝트 목록 조회
        return projectRepository.findByOwnerOrderByCreatedAtDesc(loginUser);
    }
}
