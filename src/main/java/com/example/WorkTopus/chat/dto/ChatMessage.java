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
public class ChatMessage {

    /*
     * 채팅 메시지 PK
     * DB 저장 후 생성됩니다.
     */
    private Long messageId;

    /*
     * 메시지가 속한 프로젝트 번호
     */
    private Long projectId;

    /*
     * 채팅방 식별값
     *
     * 단체 채팅:
     * project_1_group
     *
     * 개인 채팅:
     * project_1_private_3_7
     */
    private String roomId;

    /*
     * 메시지를 보낸 사용자의 USERS.USER_NUM
     *
     * 사용자 비교와 DB 연결에 사용합니다.
     * 화면에는 출력하지 않습니다.
     */
    private Long senderNum;

    /*
     * 메시지를 보낸 사용자의 이름
     *
     * 채팅 화면에는 이 값을 출력합니다.
     */
    private String senderName;

    /*
     * 실제 메시지 내용
     */
    private String message;

    /*
     * 메시지 종류
     *
     * TALK   : 일반 메시지
     * ENTER  : 입장 메시지
     * LEAVE  : 퇴장 메시지
     * SYSTEM : 시스템 메시지
     * NOTICE : 안내 메시지
     */
    @Builder.Default
    private String type = "TALK";

    /*
     * 메시지 작성 시간
     *
     * JavaScript의 new Date().toISOString() 값도
     * OffsetDateTime으로 받을 수 있습니다.
     */
    private OffsetDateTime createdAt;

    /*
     * 아직 읽지 않은 사용자 수
     *
     * 개인 채팅 읽음 표시에서 사용합니다.
     */
    private Integer unreadCount;

    /*
     * 현재 로그인 사용자가 읽었는지 여부
     */
    private Boolean readYn;
}