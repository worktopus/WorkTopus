package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.ProjectDto;
import com.example.WorkTopus.chat.service.ChatReadService;
import com.example.WorkTopus.chat.service.ProjectService;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController("chatProjectController")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    private final ChatReadService chatReadService;

    private final UserService userService;


    /*
     * 로그인 사용자가 참여 중인 프로젝트 목록
     *
     * GET /api/chat/projects
     */
    @GetMapping("/api/chat/projects")
    public List<ProjectDto> getProjects(
            Principal principal
    ) {
        Users loginUser =
                getLoginUser(principal);

        return projectService.getProjects(
                loginUser
        );
    }


    /*
     * 프로젝트 단체 채팅 읽음 처리
     *
     * POST /api/chat/projects/{projectId}/read
     */
    @PostMapping(
            "/api/chat/projects/{projectId}/read"
    )
    public ResponseEntity<Void> markProjectAsRead(
            @PathVariable Long projectId,
            Principal principal
    ) {
        Users loginUser =
                getLoginUser(principal);

        validateProjectAccess(
                projectId,
                loginUser.getUserNum()
        );

        String groupRoomId =
                createGroupRoomId(
                        projectId
                );

        /*
         * 현재 단체 채팅방의 마지막 메시지까지
         * 읽은 것으로 저장합니다.
         */
        chatReadService.markRoomAsRead(
                projectId,
                groupRoomId,
                loginUser.getUserNum()
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    /*
     * 기존 테스트용 프로젝트 생성 주소
     *
     * GET /project/create
     */
    @GetMapping("/project/create")
    public String createProject(
            Principal principal
    ) {
        /*
         * 로그인 여부 확인
         */
        getLoginUser(principal);

        projectService.createProject(
                2L,
                "WorkTopus"
        );

        return "프로젝트 생성 완료";
    }


    /*
     * 프로젝트 접근 권한 확인
     */
    private void validateProjectAccess(
            Long projectId,
            Long userNum
    ) {
        if (projectId == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        if (userNum == null) {
            throw new AccessDeniedException(
                    "로그인 사용자 번호가 없습니다."
            );
        }

        boolean projectMember =
                projectService.isProjectMember(
                        projectId,
                        userNum
                );

        if (!projectMember) {
            throw new AccessDeniedException(
                    "해당 프로젝트의 참여자가 아닙니다."
            );
        }
    }


    /*
     * 프로젝트 단체 채팅방 ID 생성
     */
    private String createGroupRoomId(
            Long projectId
    ) {
        return "project_"
                + projectId
                + "_group";
    }


    /*
     * 현재 로그인 사용자 조회
     *
     * principal.getName()에는 로그인 userId가 들어옵니다.
     */
    private Users getLoginUser(
            Principal principal
    ) {
        if (
                principal == null ||
                        principal.getName() == null ||
                        principal.getName().isBlank()
        ) {
            throw new AccessDeniedException(
                    "로그인이 필요합니다."
            );
        }

        try {
            return userService.findByUserId(
                    principal.getName()
            );

        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(
                    "로그인 사용자 정보를 찾을 수 없습니다."
            );
        }
    }
}