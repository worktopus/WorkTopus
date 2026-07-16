package com.example.WorkTopus.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "CHAT_MESSAGE",
        indexes = {
                @Index(
                        name = "IDX_CHAT_MESSAGE_ROOM",
                        columnList = "ROOM_ID"
                ),
                @Index(
                        name = "IDX_CHAT_MESSAGE_PROJECT",
                        columnList = "PROJECT_ID"
                ),
                @Index(
                        name = "IDX_CHAT_MESSAGE_ROOM_TIME",
                        columnList = "ROOM_ID, CREATED_AT"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {

    /*
     * 채팅 메시지 PK
     *
     * 현재 ChatService의 AtomicLong을 대신하여
     * DB에서 자동 생성합니다.
     */
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column(name = "MESSAGE_ID")
    private Long messageId;


    /*
     * 프로젝트 PK
     *
     * 통합 후 상대방 Projects 엔티티의
     * Projects.id 값이 들어갑니다.
     */
    @Column( name = "PROJECT_ID", nullable = false )
    private Long projectId;


    /*
     * 채팅방 식별 문자열
     *
     * 단체방:
     * project_2_group
     *
     * 개인방:
     * project_2_private_1_4
     */
    @Column( name = "ROOM_ID",
             nullable = false,
             length = 100
    )
    private String roomId;


    /*
     * 메시지를 작성한 사용자의 USERS.USER_NUM
     */
    @Column( name = "SENDER_NUM",
             nullable = false
    )
    private Long senderNum;


    /*
     * 메시지를 보낼 당시 사용자 이름
     */
    @Column( name = "SENDER_NAME",
             nullable = false,
             length = 50
    )
    private String senderName;


    /*
     * 실제 메시지 내용
     */
    @Column( name = "MESSAGE_CONTENT",
             nullable = false,
             length = 2000
    )
    private String message;


    /*
     * 메시지 종류
     *
     * TALK
     * ENTER
     * LEAVE
     * SYSTEM
     * NOTICE
     */
    @Builder.Default
    @Column( name = "MESSAGE_TYPE",
             nullable = false,
             length = 20
    )
    private String type = "TALK";


    /*
     * 메시지 작성 시간
     *
     * 메시지가 DB에 처음 저장될 때
     * Hibernate가 서버 시간을 넣습니다.
     */
    @CreationTimestamp
    @Column( name = "CREATED_AT",
             nullable = false,
             updatable = false
    )
    private OffsetDateTime createdAt;
}