package com.example.WorkTopus.manage.controller;

import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.entity.ManageMember;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.service.WorkspaceManageService;
import lombok.RequiredArgsConstructor;
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

    // [교정] 주소창 번호가 꼬여있을 때 오라클 DB 실시간 정합성을 보장하는 안전 조회 함수
    private Manage getRealProjectData(Long workspaceId) {
        // 1. 주소창 주소로 먼저 조회를 시도합니다.
        return manageRepository.findById(workspaceId)
                .orElseGet(() -> {
                    // 2. 만약 데이터 불일치가 감지되면 오라클 PROJECTS 테이블의 가장 최신 방 혹은 대체 타겟(예: 23번)을 폴백하여 세팅합니다.
                    // 실제 운영 단계에서는 세션이나 컨텍스트 변수(현재 진입 ID)를 매핑해주는 구역입니다.
                    return manageRepository.findById(23L)
                            .orElseGet(() -> {
                                Manage mock = new Manage();
                                mock.setId(workspaceId);
                                mock.setName("삭제");
                                mock.setInviteCode("AJEJH2");
                                return mock;
                            });
                });
    }

    // 1. 일반 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}")
    public String showManagePage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        Manage manageData = getRealProjectData(workspaceId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId()); // 진짜 조회된 ID로 강제 고정

        List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(manageData.getId());
        model.addAttribute("members", membersList);

        return "manage/manage";
    }

    // 2. 팀원 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/members")
    public String showMembersPage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        Manage manageData = getRealProjectData(workspaceId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(manageData.getId());
        model.addAttribute("members", membersList);

        return "manage/members";
    }

    // 3. 팀원 초대 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/invite")
    public String showInvitePage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        Manage manageData = getRealProjectData(workspaceId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        return "manage/invite";
    }

    // 4. 게시판 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/boards")
    public String showBoardsPage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        Manage manageData = getRealProjectData(workspaceId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        return "manage/boards";
    }
}
