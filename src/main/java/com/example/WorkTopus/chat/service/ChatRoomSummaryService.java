package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.dto.ChatRoomSummary;
import com.example.WorkTopus.chat.dto.ProjectDto;
import com.example.WorkTopus.chat.dto.ProjectMember;
import com.example.WorkTopus.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomSummaryService {

    private static final String GROUP =
            "GROUP";

    private static final String PRIVATE =
            "PRIVATE";


    private final ProjectService projectService;

    private final ChatService chatService;

    private final ChatReadService chatReadService;


    /*
     * 로그인 사용자의 전체 채팅방 목록 생성
     *
     * 프로젝트 단체방과 개인방을 모두 반환합니다.
     */
    public List<ChatRoomSummary> getRoomSummaries(
            Users loginUser
    ) {
        validateLoginUser(
                loginUser
        );

        /*
         * 로그인 사용자가 참여 중인 프로젝트 조회
         */
        List<ProjectDto> projects =
                projectService.getProjects(
                        loginUser
                );

        List<ChatRoomSummary> summaries =
                new ArrayList<>();


        for (ProjectDto project : projects) {

            /*
             * 프로젝트 단체 채팅방
             */
            summaries.add(
                    createGroupRoomSummary(
                            project,
                            loginUser.getUserNum()
                    )
            );


            /*
             * 프로젝트 팀원별 개인 채팅방
             */
            summaries.addAll(
                    createPrivateRoomSummaries(
                            project,
                            loginUser.getUserNum()
                    )
            );
        }


        /*
         * 마지막 메시지가 최신인 방을 위에 표시합니다.
         */
        return summaries.stream()
                .sorted(
                        roomSummaryComparator()
                )
                .toList();
    }


    /*
     * 프로젝트 단체 채팅방 요약 생성
     */
    private ChatRoomSummary createGroupRoomSummary(
            ProjectDto project,
            Long loginUserNum
    ) {
        Long projectId =
                requireProjectId(
                        project
                );

        String roomId =
                normalizeGroupRoomId(
                        project
                );

        List<ProjectMember> members =
                project.getMembers() == null
                        ? List.of()
                        : List.copyOf(
                        project.getMembers()
                );


        /*
         * 프로젝트방 마지막 메시지
         */
        ChatMessage lastMessage =
                chatService.getLastMessage(
                        roomId
                );


        /*
         * 현재 로그인 사용자의 안 읽은 메시지 수
         */
        int unreadCount =
                chatReadService.getUnreadCount(
                        roomId,
                        loginUserNum
                );


        return ChatRoomSummary.builder()
                .roomId(
                        roomId
                )
                .roomType(
                        GROUP
                )
                .projectId(
                        projectId
                )
                .roomName(
                        project.getProjectName()
                )
                .memberCount(
                        members.size()
                )
                .members(
                        members
                )
                .targetMember(
                        null
                )
                .online(
                        false
                )
                .lastMessage(
                        lastMessage
                )
                .unreadCount(
                        unreadCount
                )
                .updatedAt(
                        lastMessage == null
                                ? null
                                : lastMessage.getCreatedAt()
                )
                .build();
    }


    /*
     * 프로젝트 참여자별 개인 채팅방 생성
     */
    private List<ChatRoomSummary>
    createPrivateRoomSummaries(
            ProjectDto project,
            Long loginUserNum
    ) {
        Long projectId =
                requireProjectId(
                        project
                );

        List<ProjectMember> members =
                project.getMembers() == null
                        ? List.of()
                        : project.getMembers();


        return members.stream()

                /*
                 * 잘못된 팀원 정보 제외
                 */
                .filter(member ->
                        member != null &&
                                member.getUserNum() != null
                )

                /*
                 * 로그인 사용자 본인은 제외
                 */
                .filter(member ->
                        !loginUserNum.equals(
                                member.getUserNum()
                        )
                )

                /*
                 * 상대방마다 개인 채팅방 요약 생성
                 */
                .map(member ->
                        createPrivateRoomSummary(
                                projectId,
                                loginUserNum,
                                member
                        )
                )
                .toList();
    }


    /*
     * 개인 채팅방 한 개의 요약 생성
     */
    private ChatRoomSummary createPrivateRoomSummary(
            Long projectId,
            Long loginUserNum,
            ProjectMember targetMember
    ) {
        String roomId =
                createPrivateRoomId(
                        projectId,
                        loginUserNum,
                        targetMember.getUserNum()
                );


        /*
         * 개인 채팅방의 마지막 메시지
         *
         * 새로고침 후 사라졌던 정보가
         * 이 부분을 통해 다시 조회됩니다.
         */
        ChatMessage lastMessage =
                chatService.getLastMessage(
                        roomId
                );


        /*
         * 개인 채팅방 안 읽은 메시지 수
         */
        int unreadCount =
                chatReadService.getUnreadCount(
                        roomId,
                        loginUserNum
                );


        return ChatRoomSummary.builder()
                .roomId(
                        roomId
                )
                .roomType(
                        PRIVATE
                )
                .projectId(
                        projectId
                )
                .roomName(
                        targetMember.getName()
                )
                .memberCount(
                        2
                )
                .members(
                        List.of()
                )
                .targetMember(
                        targetMember
                )
                .online(
                        targetMember.isOnline()
                )
                .lastMessage(
                        lastMessage
                )
                .unreadCount(
                        unreadCount
                )
                .updatedAt(
                        lastMessage == null
                                ? null
                                : lastMessage.getCreatedAt()
                )
                .build();
    }


    /*
     * 프로젝트 단체방 ID 확인
     */
    private String normalizeGroupRoomId(
            ProjectDto project
    ) {
        String roomId =
                project.getGroupRoomId();

        if (
                roomId != null &&
                        !roomId.isBlank()
        ) {
            return roomId.trim();
        }

        return createGroupRoomId(
                requireProjectId(
                        project
                )
        );
    }


    /*
     * 프로젝트 단체 채팅방 ID 생성
     *
     * project_2_group
     */
    private String createGroupRoomId(
            Long projectId
    ) {
        return "project_" +
                projectId +
                "_group";
    }


    /*
     * 개인 채팅방 ID 생성
     *
     * 사용자 번호가 1번과 4번이면
     * 항상 project_2_private_1_4가 됩니다.
     */
    private String createPrivateRoomId(
            Long projectId,
            Long firstUserNum,
            Long secondUserNum
    ) {
        long small =
                Math.min(
                        firstUserNum,
                        secondUserNum
                );

        long large =
                Math.max(
                        firstUserNum,
                        secondUserNum
                );

        return "project_" +
                projectId +
                "_private_" +
                small +
                "_" +
                large;
    }


    /*
     * 프로젝트 번호 확인
     */
    private Long requireProjectId(
            ProjectDto project
    ) {
        if (
                project == null ||
                        project.getProjectId() == null
        ) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        return project.getProjectId();
    }


    /*
     * 채팅방 목록 정렬
     *
     * 1. 마지막 메시지가 최신인 방
     * 2. 마지막 메시지가 없으면 프로젝트방 우선
     * 3. 채팅방 이름순
     */
    private Comparator<ChatRoomSummary>
    roomSummaryComparator() {
        return Comparator
                .comparing(
                        ChatRoomSummary::getUpdatedAt,

                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
                .thenComparingInt(summary ->
                        GROUP.equals(
                                summary.getRoomType()
                        )
                                ? 0
                                : 1
                )
                .thenComparing(
                        summary ->
                                String.valueOf(
                                        summary.getRoomName()
                                )
                );
    }


    /*
     * 로그인 사용자 검증
     */
    private void validateLoginUser(
            Users loginUser
    ) {
        if (
                loginUser == null ||
                        loginUser.getUserNum() == null
        ) {
            throw new IllegalArgumentException(
                    "로그인 사용자 정보가 없습니다."
            );
        }
    }
}