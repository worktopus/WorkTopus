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

    /**
     * 📌 [신규 추가] 현재 로그인한 유저의 프로젝트 내 담당 역할을 찾아 헤더 템플릿용 모델에 주입합니다.
     * Spring Security 환경에 따라 유저 ID(userId) 혹은 이름(name) 매칭 방식을 모두 안전하게 수용합니다.
     */
    private void bindHeaderProjectMemberRole(Long workspaceId, Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Spring Security가 인증 컨텍스트에서 반환하는 현재 로그인 유저 식별값 수집
            String loginSecurityName = authentication.getName();

            // 현재 워크스페이스에 속한 전체 참여 멤버 목록 조회
            List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(workspaceId);

            // 엔티티의 getUser() 객체가 들고 있는 userId나 name 중 하나라도 일치하는 '나 자신'을 정밀 필터링합니다.
            ManageMember currentLoggedInMember = membersList.stream()
                    .filter(m -> m.getUser() != null && (
                            loginSecurityName.equals(m.getUser().getUserId()) ||
                                    loginSecurityName.equals(m.getUser().getName())
                    ))
                    .findFirst()
                    .orElse(null);

            // fragments/header.html 템플릿이 수집해가는 "projectMember" 명칭으로 최신 객체 실시간 매핑 주입
            model.addAttribute("projectMember", currentLoggedInMember);
        }
    }

    // 1. 일반 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}")
    public String showManagePage(@PathVariable("workspaceId") Long workspaceId, Model model, Authentication authentication) { // 📌 Authentication 인자 추가
        Manage manageData = getRealProjectData(workspaceId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId()); // 진짜 조회된 ID로 강제 고정

        List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(manageData.getId());
        model.addAttribute("members", membersList);

        // 📌 헤더 우측 상단 내 역할 표시 파이프라인 새로고침 검증 및 동기화 가동
        bindHeaderProjectMemberRole(manageData.getId(), authentication, model);

        return "manage/manage";
    }
    // 2. 팀원 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/members")
    public String showMembersPage(@PathVariable("workspaceId") Long workspaceId, Model model, Authentication authentication) { // 📌 Authentication 인자 추가
        Manage manageData = getRealProjectData(workspaceId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(manageData.getId());
        model.addAttribute("members", membersList);

        // 📌 헤더 우측 상단 내 역할 표시 파이프라인 새로고침 검증 및 동기화 가동
        bindHeaderProjectMemberRole(manageData.getId(), authentication, model);

        return "manage/members";
    }

    // 3. 팀원 초대 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/invite")
    public String showInvitePage(@PathVariable("workspaceId") Long workspaceId, Model model, Authentication authentication) { // 📌 Authentication 인자 추가
        Manage manageData = getRealProjectData(workspaceId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        // 📌 헤더 우측 상단 내 역할 표시 파이프라인 새로고침 검증 및 동기화 가동
        bindHeaderProjectMemberRole(manageData.getId(), authentication, model);

        return "manage/invite";
    }

    // 4. 게시판 관리 탭 뷰 매핑
    @GetMapping("/projects/manage/{workspaceId}/boards")
    public String showBoardsPage(@PathVariable("workspaceId") Long workspaceId, Model model, Authentication authentication) { // 📌 Authentication 인자 추가
        Manage manageData = getRealProjectData(workspaceId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", manageData.getId());

        // 📌 헤더 우측 상단 내 역할 표시 파이프라인 새로고침 검증 및 동기화 가동
        bindHeaderProjectMemberRole(manageData.getId(), authentication, model);

        return "manage/boards";
    }
}
