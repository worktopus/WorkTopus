package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRead {

    /*
     * 읽음 정보 PK
     *
     * 현재 메모리 단계에서는 없어도 되지만,
     * 이후 CHAT_READ 테이블 연결을 위해 준비합니다.
     */
    private Long readId;

    /*
     * 프로젝트 번호
     */
    private Long projectId;

    /*
     * 읽음 처리할 채팅방 ID
     *
     * 단체 채팅:
     * project_2_group
     *
     * 개인 채팅:
     * project_2_private_1_4
     */
    private String roomId;

    /*
     * 채팅방을 읽은 사용자의 USERS.USER_NUM
     */
    private Long userNum;

    /*
     * 이 사용자가 마지막으로 읽은 메시지 번호
     *
     * 예:
     * lastReadMessageId = 15
     *
     * messageId 15번까지 읽었다는 뜻입니다.
     */
    private Long lastReadMessageId;

    /*
     * 마지막 읽음 처리 시간
     */
    private OffsetDateTime readAt;
}