package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomSummary {

    /*
     * 채팅방 ID
     *
     * 프로젝트:
     * project_2_group
     *
     * 개인:
     * project_2_private_1_4
     */
    private String roomId;

    /*
     * 채팅방 종류
     *
     * GROUP
     * PRIVATE
     */
    private String roomType;

    /*
     * 채팅방이 속한 프로젝트 번호
     */
    private Long projectId;

    /*
     * 목록에 표시할 채팅방 이름
     *
     * GROUP:
     * WorkTopus
     *
     * PRIVATE:
     * 김철수
     */
    private String roomName;

    /*
     * 프로젝트 채팅방 참여 인원
     *
     * 개인 채팅방은 2명입니다.
     */
    @Builder.Default
    private int memberCount = 0;

    /*
     * 프로젝트 채팅방 참여자 목록
     *
     * 프로젝트 아이콘 생성에 사용합니다.
     */
    @Builder.Default
    private List<ProjectMember> members =
            new ArrayList<>();

    /*
     * 개인 채팅 상대방
     *
     * 프로젝트 채팅방에서는 null입니다.
     */
    private ProjectMember targetMember;

    /*
     * 개인 채팅 상대방 접속 여부
     *
     * 프로젝트 채팅방에서는 사용하지 않습니다.
     */
    @Builder.Default
    private boolean online = false;

    /*
     * 해당 채팅방의 마지막 메시지
     */
    private ChatMessage lastMessage;

    /*
     * 현재 로그인 사용자가 읽지 않은 메시지 수
     */
    @Builder.Default
    private int unreadCount = 0;

    /*
     * 채팅방 목록 정렬 기준 시간
     *
     * 보통 마지막 메시지 작성 시간과 같습니다.
     */
    private OffsetDateTime updatedAt;
}