package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.dto.ProjectDto;
import com.example.WorkTopus.entity.ProjectMember;
import com.example.WorkTopus.entity.ProjectRole;
import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("chatProjectService")
@RequiredArgsConstructor
public class ProjectService {

    /* 프로젝트 채팅방 관리 */
    private final ChatRoomService chatRoomService;

    /* 채팅 메시지 조회 */
    private final ChatService chatService;

    /* 채팅 읽음 정보와 안 읽은 메시지 수 계산 */
    private final ChatReadService chatReadService;

    /* 실제 PROJECT_MEMBER 테이블 조회 */
    private final ProjectMemberRepository projectMemberRepository;

    /* 실제 프로젝트 참여자 조회  */
    private final ProjectMemberService
            projectMemberService;

    /*
     * 프로젝트 생성 시 기본 단체채팅방 생성
     *
     * 실제 프로젝트 생성 기능과의 자동 연결은
     * 이후 단계에서 처리합니다.
     */
    public void createProject(
            Long projectId,
            String projectName
    ) {
        validateProject(
                projectId,
                projectName
        );

        chatRoomService.createProjectGroupRoom(
                projectId,
                projectName.trim()
        );
    }


    /*
     * 로그인 사용자가 참여 중인 실제 프로젝트 목록 조회
     *
     * USERS
     * → PROJECT_MEMBER
     * → PROJECTS
     */
    @Transactional(readOnly = true)
    public List<ProjectDto> getProjects(
            Users loginUser
    ) {
        validateLoginUser(loginUser);

        List<ProjectMember> projectMembers =
                projectMemberRepository
                        .findByUserOrderByJoinedAtDesc(
                                loginUser
                        );

        return projectMembers
                .stream()
                .map(projectMember ->
                        createProjectDto(
                                projectMember.getProject(),
                                loginUser.getUserNum()
                        )
                )
                .toList();
    }


    /*
     * 실제 Projects Entity를
     * 채팅 화면용 ProjectDto로 변환
     */
    private ProjectDto createProjectDto(
            Projects project,
            Long loginUserNum
    ) {
        if (
                project == null ||
                        project.getId() == null
        ) {
            throw new IllegalArgumentException(
                    "프로젝트 정보가 올바르지 않습니다."
            );
        }

        Long projectId =
                project.getId();

        String groupRoomId =
                createGroupRoomId(
                        projectId
                );

        /*
         * 메모리 채팅방 목록에 단체방이 없으면 생성합니다.
         *
         * 채팅방 ID 예:
         * project_15_group
         */
        if (
                chatRoomService.getRoom(
                        groupRoomId
                ) == null
        ) {
            chatRoomService.createProjectGroupRoom(
                    projectId,
                    project.getName()
            );
        }

        /*
         * 해당 프로젝트 단체채팅의 마지막 메시지
         */
        ChatMessage lastMessage =
                chatService.getLastMessage(
                        groupRoomId
                );

        /*
         * 현재 로그인 사용자가 읽지 않은 메시지 수
         */
        int unreadCount =
                chatReadService.getUnreadCount(
                        groupRoomId,
                        loginUserNum
                );

        return ProjectDto.builder()
                .projectId(
                        projectId
                )
                .projectName(
                        project.getName()
                )
                .description(
                        project.getDescription()
                )
                .groupRoomId(
                        groupRoomId
                )
                .unreadCount(
                        unreadCount
                )
                .lastMessage(
                        lastMessage
                )

                /* 실제 PROJECT_MEMBER와 USERS에서
                   프로젝트 참여자 목록을 조회합니다.
                 */
                .members(
                        projectMemberService.getMembers(
                                projectId
                        )
                )
                .build();
    }


    /*
     * 실제 PROJECT_MEMBER 테이블 기준
     * 프로젝트 OWNER 여부 확인
     */
    public boolean isProjectOwner(
            Long projectId,
            Long userNum
    ) {
        if (
                projectId == null ||
                        userNum == null
        ) {
            return false;
        }

        return projectMemberRepository
                .existsByProject_IdAndUser_UserNumAndRole(
                        projectId,
                        userNum,
                        ProjectRole.OWNER
                );
    }


    /*
     * 실제 PROJECT_MEMBER 테이블 기준
     * 프로젝트 참여 여부 확인
     */
    public boolean isProjectMember(
            Long projectId,
            Long userNum
    ) {
        if (
                projectId == null ||
                        userNum == null
        ) {
            return false;
        }

        return projectMemberRepository
                .existsByProject_IdAndUser_UserNum(
                        projectId,
                        userNum
                );
    }


    /*
     * 프로젝트 단체채팅방 ID 생성
     */
    private String createGroupRoomId(
            Long projectId
    ) {
        return "project_"
                + projectId
                + "_group";
    }


    /*
     * 프로젝트 생성 요청 검증
     */
    private void validateProject(
            Long projectId,
            String projectName
    ) {
        if (projectId == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        if (
                projectName == null ||
                        projectName.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "프로젝트 이름이 없습니다."
            );
        }
    }


    /*
     * 로그인 사용자 검증
     */
    private void validateLoginUser(
            Users loginUser
    ) {
        if (loginUser == null) {
            throw new IllegalArgumentException(
                    "로그인 사용자 정보가 없습니다."
            );
        }

        if (loginUser.getUserNum() == null) {
            throw new IllegalArgumentException(
                    "로그인 사용자 번호가 없습니다."
            );
        }

        if (
                loginUser.getUserId() == null ||
                        loginUser.getUserId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "로그인 아이디가 없습니다."
            );
        }

        if (
                loginUser.getName() == null ||
                        loginUser.getName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "로그인 사용자 이름이 없습니다."
            );
        }
    }
}