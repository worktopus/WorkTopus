package com.example.WorkTopus.manage.controller;

import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.entity.ManageMember;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.service.WorkspaceManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // 📌 Spring Security 인증 처리를 위한 임포트 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ManageViewController {

    private final ManageRepository manageRepository;
    private final WorkspaceManageService workspaceManageService;



    // [교정] 주소창 번호가 꼬여있을 때 오라클 DB 실시간 정합성을 보장하는 안전 조회 함수 (원본 보존)
    private Manage getRealProjectData(Long workspaceId) {
        return manageRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 프로젝트가 존재하지 않습니다."
                        )
                );
    }

    private ManageMember validateOwner(
            Long workspaceId,
            Authentication authentication
    ) {
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "로그인이 필요합니다."
            );
        }

        return workspaceManageService.validateProjectOwner(
                workspaceId,
                authentication.getName()
        );
    }


    // 1. 일반 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}")
    public String showManagePage(@PathVariable("workspaceId") Long workspaceId, Model model, Authentication authentication) { // 📌 Authentication 인자 추가
        ManageMember currentMember =
                validateOwner(workspaceId, authentication);

        Manage manageData = getRealProjectData(workspaceId);

        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId()); // 진짜 조회된 ID로 강제 고정
        model.addAttribute("projectMember", currentMember);
        model.addAttribute(
                "members",
                workspaceManageService.getWorkspaceMembers(workspaceId)
        );


        return "manage/manage";
    }
    // 2. 팀원 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/members")
    public String showMembersPage(@PathVariable("workspaceId") Long workspaceId, Model model, Authentication authentication) { // 📌 Authentication 인자 추가
        ManageMember currentMember =
                validateOwner(workspaceId, authentication);

        Manage manageData = getRealProjectData(workspaceId);

        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());
        model.addAttribute("projectMember", currentMember);
        model.addAttribute(
                "members",
                workspaceManageService.getWorkspaceMembers(workspaceId)
        );


        return "manage/members";
    }

    // 3. 팀원 초대 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/invite")
    public String showInvitePage(@PathVariable("workspaceId") Long workspaceId, Model model, Authentication authentication) { // 📌 Authentication 인자 추가
        ManageMember currentMember =
                validateOwner(workspaceId, authentication);

        Manage manageData = getRealProjectData(workspaceId);

        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());
        model.addAttribute("projectMember", currentMember);

        return "manage/invite";
    }

    // 4. 게시판 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/boards")
    public String showBoardsPage(@PathVariable("workspaceId") Long workspaceId, Model model, Authentication authentication) { // 📌 Authentication 인자 추가
        ManageMember currentMember =
                validateOwner(workspaceId, authentication);

        Manage manageData = getRealProjectData(workspaceId);

        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());
        model.addAttribute("projectMember", currentMember);

        return "manage/boards";
    }
}
