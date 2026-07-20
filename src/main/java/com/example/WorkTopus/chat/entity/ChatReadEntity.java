package com.example.WorkTopus.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "CHAT_READ",

        /*
         * 한 사용자는 한 채팅방에
         * 읽음 상태 한 건만 가질 수 있습니다.
         */
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_CHAT_READ_ROOM_USER",
                        columnNames = {
                                "ROOM_ID",
                                "USER_NUM"
                        }
                )
        },

        indexes = {
                @Index(
                        name = "IDX_CHAT_READ_ROOM",
                        columnList = "ROOM_ID"
                ),
                @Index(
                        name = "IDX_CHAT_READ_USER",
                        columnList = "USER_NUM"
                ),
                @Index(
                        name = "IDX_CHAT_READ_PROJECT",
                        columnList = "PROJECT_ID"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReadEntity {

    /*
     * 읽음 정보 PK
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "READ_ID")
    private Long readId;


    /*
     * 프로젝트 PK
     *
     * 통합 후 Projects.id 값이 들어갑니다.
     */
    @Column(
            name = "PROJECT_ID",
            nullable = false
    )
    private Long projectId;


    /*
     * 채팅방 ID
     *
     * project_2_group
     * project_2_private_1_3
     */
    @Column(
            name = "ROOM_ID",
            nullable = false,
            length = 100
    )
    private String roomId;


    /*
     * 읽은 사용자의 USERS.USER_NUM
     */
    @Column(
            name = "USER_NUM",
            nullable = false
    )
    private Long userNum;


    /*
     * 사용자가 마지막으로 읽은 메시지 번호
     *
     * CHAT_MESSAGE.MESSAGE_ID 값입니다.
     */
    @Column(
            name = "LAST_READ_MESSAGE_ID",
            nullable = false
    )
    private Long lastReadMessageId;


    /*
     * 마지막 읽음 처리 시간
     */
    @Column(
            name = "READ_AT",
            nullable = false
    )
    private OffsetDateTime readAt;
}