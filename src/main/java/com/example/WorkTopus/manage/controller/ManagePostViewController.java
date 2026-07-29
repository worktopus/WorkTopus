package com.example.WorkTopus.manage.controller;

import com.example.WorkTopus.manage.dto.ManagePostPageDto;
import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.entity.ManageMember;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.service.ManagePostService;
import com.example.WorkTopus.manage.service.WorkspaceManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class ManagePostViewController {

    private final ManageRepository manageRepository;
    private final ManagePostService managePostService;
    private final WorkspaceManageService workspaceManageService;

    @GetMapping("/projects/manage/{projectId}/posts")
    public String showPostManagementPage(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication,
            Model model
    ) {
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "로그인이 필요합니다."
            );
        }

        ManageMember currentMember =
                workspaceManageService.validateProjectOwner(
                        projectId,
                        authentication.getName()
                );

        model.addAttribute("projectMember", currentMember);

        Manage project = manageRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다."));

        ManagePostPageDto postPage = managePostService.getPosts(
                projectId,
                category,
                keyword,
                page,
                size
        );

        List<Integer> pageNumbers = postPage.getTotalPages() == 0
                ? List.of()
                : IntStream.range(0, postPage.getTotalPages()).boxed().toList();

        model.addAttribute("project", project);
        model.addAttribute("projectId", projectId);
        model.addAttribute("postStats", managePostService.getStatistics(projectId));
        model.addAttribute("postPage", postPage);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);

        // 기존 공통 헤더가 projectMember를 요구한다면
        // ManageViewController의 bindHeaderProjectMemberRole 로직을 공통 컴포넌트로 분리해 여기서 호출하세요.

        return "manage/posts";
    }
}
