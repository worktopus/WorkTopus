package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.ProjectMember;
import com.example.WorkTopus.chat.service.ProjectAccessService;
import com.example.WorkTopus.chat.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService
            memberService;

    private final ProjectAccessService
            projectAccessService;


    /*
     * 프로젝트 팀원 목록
     *
     * GET /member/{projectId}
     */
    @GetMapping("/{projectId}")
    public List<ProjectMember> list(
            @PathVariable Long projectId,
            Principal principal
    ) {
        /*
         * 로그인 사용자가 해당 프로젝트에
         * 참여 중인 경우에만 팀원 목록을 반환합니다.
         */
        projectAccessService
                .requireProjectMember(
                        projectId,
                        principal
                );

        return memberService.getMembers(
                projectId
        );
    }
}