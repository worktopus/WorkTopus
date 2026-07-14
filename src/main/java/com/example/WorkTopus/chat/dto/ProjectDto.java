package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    /* 프로젝트 PK  */
    private Long projectId;

    /* 화면에 표시할 프로젝트 이름  */
    private String projectName;

    /*
     * 프로젝트 설명
     */
    private String description;

    /*
     * 프로젝트 단체 채팅방 ID
     *
     * 예:
     * project_1_group
     */
    private String groupRoomId;

    /*
     * 현재 로그인 사용자가 읽지 않은 메시지 수
     */
    @Builder.Default
    private int unreadCount = 0;

    /*
     * 프로젝트 단체 채팅의 마지막 메시지
     */
    private ChatMessage lastMessage;

    /*
     * 프로젝트 참여 팀원
     *
     * userNum으로 사용자를 식별하고
     * 화면에는 name을 출력합니다.
     */
    @Builder.Default
    private List<ProjectMember> members =
            new ArrayList<>();
}