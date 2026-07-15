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

    @GetMapping("/manage/{workspaceId}")
    public String showManagePage(@PathVariable("workspaceId") Long workspaceId, Model model) {

        Manage manageData = manageRepository.findById(workspaceId)
                .orElseGet(() -> {
                    Manage mock = new Manage();
                    mock.setId(workspaceId);
                    mock.setName("Worktopus");
                    mock.setVisibility("PUBLIC");
                    return mock;
                });

        model.addAttribute("project", manageData);

        // [추가] 오라클 DB에서 이 워크스페이스에 참여 중인 팀원 목록을 실시간으로 가져와 넘깁니다.
        List<ManageMember> membersList = workspaceManageService.getWorkspaceMembers(workspaceId);
        model.addAttribute("members", membersList);

        return "manage/manage";
    }
}
