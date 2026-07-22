package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.ProjectDto;
import com.example.WorkTopus.chat.service.ChatReadService;
import com.example.WorkTopus.chat.service.ProjectAccessService;
import com.example.WorkTopus.chat.service.ProjectService;
import com.example.WorkTopus.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;


@RestController("chatProjectController")
@RequiredArgsConstructor
public class ProjectController {

    /*
     * 로그인 사용자가 참여 중인
     * 프로젝트와 프로젝트 팀원 정보를 조회합니다.
     */
    private final ProjectService projectService;


    /*
     * 프로젝트 채팅방의 읽음 위치를
     * DB에 저장하는 서비스입니다.
     */
    private final ChatReadService chatReadService;


    /*
     * 로그인 사용자 조회와
     * 프로젝트 참여자 권한 검사를 공통으로 처리합니다.
     */
    private final ProjectAccessService projectAccessService;


    /*
     * 로그인 사용자가 참여 중인 프로젝트 목록
     *
     * GET /api/chat/projects
     */
    @GetMapping("/api/chat/projects")
    public List<ProjectDto> getProjects(
            Principal principal
    ) {

        /*
         * 현재 Spring Security에 로그인한
         * 실제 사용자 정보를 조회합니다.
         *
         * 브라우저에서 userNum을 따로 전달받지 않고
         * 서버의 로그인 정보를 기준으로 조회합니다.
         */
        Users loginUser =
                projectAccessService
                        .getLoginUser(
                                principal
                        );


        /*
         * 로그인 사용자가 실제로 참여 중인
         * 프로젝트 목록만 반환합니다.
         */
        return projectService.getProjects(
                loginUser
        );
    }


    /*
     * 프로젝트 단체 채팅 읽음 처리
     *
     * POST /api/chat/projects/{projectId}/read
     *
     * 예:
     *
     * POST /api/chat/projects/22/read
     */
    @PostMapping(
            "/api/chat/projects/{projectId}/read"
    )
    public ResponseEntity<Void> markProjectAsRead(
            @PathVariable Long projectId,
            Principal principal
    ) {

        /*
         * 현재 로그인 사용자가 해당 프로젝트의
         * 실제 참여자인지 검사합니다.
         *
         * 참여자가 아니면 AccessDeniedException이 발생하여
         * 읽음 처리가 실행되지 않습니다.
         *
         * 참여자라면 실제 로그인 Users 객체를 반환합니다.
         */
        Users loginUser =
                projectAccessService
                        .requireProjectMember(
                                projectId,
                                principal
                        );


        /*
         * 프로젝트 단체 채팅방 ID를 생성합니다.
         *
         * projectId = 22
         * → project_22_group
         */
        String groupRoomId =
                createGroupRoomId(
                        projectId
                );


        /*
         * 해당 프로젝트 단체방의 마지막 메시지까지
         * 현재 로그인 사용자가 읽은 것으로 저장합니다.
         *
         * 프런트에서 전달한 userNum이 아니라
         * 실제 로그인 사용자의 userNum을 사용합니다.
         */
        chatReadService.markRoomAsRead(
                projectId,
                groupRoomId,
                loginUser.getUserNum()
        );


        /*
         * 읽음 처리가 정상 완료되었으며
         * 반환할 내용은 없다는 의미의 204 응답입니다.
         */
        return ResponseEntity
                .noContent()
                .build();
    }


    /*
     * 프로젝트 단체 채팅방 ID 생성
     *
     * projectId = 22
     * → project_22_group
     */
    private String createGroupRoomId(
            Long projectId
    ) {
        return "project_"
                + projectId
                + "_group";
    }
}