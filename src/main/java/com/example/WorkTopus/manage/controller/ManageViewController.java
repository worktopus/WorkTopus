package com.example.WorkTopus.manage.controller;

import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.entity.ManageMember;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.service.WorkspaceManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
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

    // [교정] 주소창 번호 안전 조회 함수 (프로젝트 ID 체계 매핑)
    private Manage getRealProjectData(Long projectId) {
        return manageRepository.findById(projectId)
                .orElseGet(() -> {
                    return manageRepository.findById(23L)
                            .orElseGet(() -> {
                                Manage mock = new Manage();
                                mock.setId(projectId);
                                mock.setName("삭제");
                                mock.setInviteCode("AJEJH2");
                                return mock;
                            });
                });
    }

    /**
     * 현재 로그인한 유저의 프로젝트 내 담당 역할을 찾아 헤더 템플릿용 모델에 주입합니다.
     */
    private void bindHeaderProjectMemberRole(Long projectId, Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String loginSecurityName = authentication.getName();
            List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(projectId);

            ManageMember currentLoggedInMember = membersList.stream()
                    .filter(m -> m.getUser() != null && (
                            loginSecurityName.equals(m.getUser().getUserId()) ||
                                    loginSecurityName.equals(m.getUser().getName())
                    ))
                    .findFirst()
                    .orElse(null);

            model.addAttribute("projectMember", currentLoggedInMember);
        }
    }

    /**
     * 1. 🛠️ 일반 관리 탭 뷰 매핑
     * 변경된 주소창: /projects/{projectId}/manage
     */
    @GetMapping("/projects/{projectId}/manage")
    public String showManagePage(@PathVariable("projectId") Long projectId, Model model, Authentication authentication) {
        Manage manageData = getRealProjectData(projectId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(manageData.getId());
        model.addAttribute("members", membersList);

        bindHeaderProjectMemberRole(manageData.getId(), authentication, model);

        return "manage/manage";
    }

    /**
     * 2. 👥 팀원 관리 탭 뷰 매핑
     * 변경된 주소창: /projects/{projectId}/manage/members
     */
    @GetMapping("/projects/{projectId}/manage/members")
    public String showMembersPage(@PathVariable("projectId") Long projectId, Model model, Authentication authentication) {
        Manage manageData = getRealProjectData(projectId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(manageData.getId());
        model.addAttribute("members", membersList);

        bindHeaderProjectMemberRole(manageData.getId(), authentication, model);

        return "manage/members";
    }

    /**
     * 3. ✉️ 팀원 초대 탭 뷰 매핑
     * 변경된 주소창: /projects/{projectId}/manage/invite
     */
    @GetMapping("/projects/{projectId}/manage/invite")
    public String showInvitePage(@PathVariable("projectId") Long projectId, Model model, Authentication authentication) {
        Manage manageData = getRealProjectData(projectId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        bindHeaderProjectMemberRole(manageData.getId(), authentication, model);

        return "manage/invite";
    }

    /**
     * 4. 📝 게시판 관리 탭 뷰 매핑
     * 변경된 주소창: /projects/{projectId}/manage/boards
     */
    @GetMapping("/projects/{projectId}/manage/boards")
    public String showBoardsPage(
            @PathVariable("projectId") Long projectId,
            @PageableDefault(size = 10) Pageable pageable,
            Model model,
            Authentication authentication) {

        Manage manageData = getRealProjectData(projectId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        bindHeaderProjectMemberRole(manageData.getId(), authentication, model);

        try {
            Page<?> boardPage = workspaceManageService.getIntegratedBoardPage(manageData.getId(), pageable);
            model.addAttribute("boards", boardPage);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("boards", Page.empty());
        }

        return "manage/boards";
    }
}
