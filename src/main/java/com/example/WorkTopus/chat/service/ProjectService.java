package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.dto.ProjectDto;
import com.example.WorkTopus.chat.dto.ProjectMember;
import com.example.WorkTopus.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("chatProjectService")
@RequiredArgsConstructor
public class ProjectService {

    /*
     * 실제 프로젝트 DB 연결 전까지 사용하는
     * 임시 프로젝트 정보입니다.
     */
    private static final Long TEMP_PROJECT_ID = 2L;

    private static final String TEMP_PROJECT_NAME = "WorkTopus";

    private static final String TEMP_PROJECT_DESCRIPTION = "WorkTopus 협업 프로젝트";


    private final ChatRoomService chatRoomService;

    private final ProjectMemberService projectMemberService;

    private final ChatService chatService;

    /*
     * 채팅 읽음 정보 및 안 읽은 메시지 수 계산
     */
    private final ChatReadService chatReadService;


    /*
     * 프로젝트 생성 시 기본 단체 채팅방 생성
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
     * 로그인 사용자가 참여하는 프로젝트 목록 조회
     *
     * 현재는 WorkTopus 임시 프로젝트 하나를 반환합니다.
     */
    public List<ProjectDto> getProjects(
            Users loginUser
    ) {
        validateLoginUser(
                loginUser
        );

        /*
         * 로그인 사용자를 임시 프로젝트 팀원으로 등록하거나
         * 기존 팀원 정보를 갱신합니다.
         */
        registerLoginUser(
                TEMP_PROJECT_ID,
                loginUser
        );

        ProjectDto project =
                createTemporaryProject(
                        TEMP_PROJECT_ID,
                        loginUser.getUserNum()
                );

        return List.of(
                project
        );
    }


    /*
     * 임시 프로젝트 DTO 생성
     */
    private ProjectDto createTemporaryProject(
            Long projectId,
            Long loginUserNum
    ) {
        String groupRoomId =
                createGroupRoomId(
                        projectId
                );

        /*
         * 프로젝트 단체 채팅방이 없으면 생성합니다.
         */
        if (
                chatRoomService.getRoom(
                        groupRoomId
                ) == null
        ) {
            chatRoomService.createProjectGroupRoom(
                    projectId,
                    TEMP_PROJECT_NAME
            );
        }

        List<ProjectMember> members =
                projectMemberService.getMembers(
                        projectId
                );

        /*
         * 프로젝트 단체 채팅의 마지막 메시지입니다.
         * 목록에 작성자, 내용, 시간이 출력됩니다.
         */
        ChatMessage lastMessage =
                chatService.getLastMessage(
                        groupRoomId
                );

        /*
         * 현재 로그인 사용자가 읽지 않은
         * 프로젝트 단체 채팅 메시지 수입니다.
         *
         * 본인이 보낸 메시지는 제외됩니다.
         */
        int unreadCount =
                chatReadService.getUnreadCount(
                        groupRoomId,
                        loginUserNum
                );

        return ProjectDto.builder()
                .projectId( projectId )
                .projectName( TEMP_PROJECT_NAME )
                .description( TEMP_PROJECT_DESCRIPTION )
                .groupRoomId( groupRoomId )
                .unreadCount(
                        unreadCount
                )
                .lastMessage(
                        lastMessage
                )
                .members(
                        members
                )
                .build();
    }


    /*
     * 로그인 사용자 등록 또는 갱신
     *
     * 기존 팀원이 owner이면 owner 상태를 유지합니다.
     */
    private void registerLoginUser(
            Long projectId,
            Users loginUser
    ) {
        ProjectMember existingMember =
                projectMemberService.getMember(
                        projectId,
                        loginUser.getUserNum()
                );

        boolean owner =
                existingMember != null &&
                        existingMember.isOwner();

        projectMemberService.registerLoginMember(
                projectId,
                loginUser.getUserNum(),
                loginUser.getUserId(),
                loginUser.getName(),
                owner
        );
    }


    /*
     * 프로젝트 owner 여부 확인
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

        return projectMemberService
                .isProjectOwner(
                        projectId,
                        userNum
                );
    }


    /*
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

        return projectMemberService
                .isProjectMember(
                        projectId,
                        userNum
                );
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