package com.example.WorkTopus.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manage")
public class ManageViewController {

    /**
     * [4번 기능] 설정 메인 대시보드 화면 진입
     * 호출 주소: http://localhost:8080/manage/{workspaceId}
     */
    @GetMapping("/{workspaceId}")
    public String mainDashboardPage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        return "manage/manage";
    }

    /**
     * [4-1 기능] 워크스페이스 일반 관리 화면 이동
     * 호출 주소: http://localhost:8080/manage/{workspaceId}/general
     */
    @GetMapping("/{workspaceId}/general")
    public String generalManagementPage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        return "manage/general";
    }

    /**
     * [4-2-1 기능] 팀원 초대 화면 이동
     * 호출 주소: http://localhost:8080/manage/{workspaceId}/invite
     */
    @GetMapping("/{workspaceId}/invite")
    public String memberInvitePage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        return "manage/invite";
    }

    /**
     * [4-2 및 4-2-2 기능] 팀원 목록 및 역할 관리 화면 이동
     * 호출 주소: http://localhost:8080/manage/{workspaceId}/members
     */
    @GetMapping("/{workspaceId}/members")
    public String memberManagementPage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        return "manage/members";
    }

    /**
     * [4-3 기능] 내부 게시판 관리 화면 이동
     * 호출 주소: http://localhost:8080/manage/{workspaceId}/boards
     */
    @GetMapping("/{workspaceId}/boards")
    public String boardManagementPage(@PathVariable("workspaceId") Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        return "manage/boards";
    }
}
