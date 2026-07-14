package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.dto.ChatRead;
import com.example.WorkTopus.chat.entity.ChatReadEntity;
import com.example.WorkTopus.chat.repository.ChatReadJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatReadService {

    /*
     * 기존 ConcurrentHashMap 대신
     * Oracle의 CHAT_READ 테이블을 사용합니다.
     */
    private final ChatReadJpaRepository
            chatReadJpaRepository;

    private final ChatService chatService;


    /*
     * 현재 채팅방의 마지막 메시지까지 읽음 처리
     */
    @Transactional
    public ChatRead markRoomAsRead(
            Long projectId,
            String roomId,
            Long userNum
    ) {
        validateRequest(
                projectId,
                roomId,
                userNum
        );

        String normalizedRoomId =
                roomId.trim();

        /*
         * 현재 채팅방의 마지막 메시지를 조회합니다.
         */
        ChatMessage lastMessage =
                chatService.getLastMessage(
                        normalizedRoomId
                );

        /*
         * 메시지가 하나도 없는 방은
         * 0번까지 읽은 것으로 저장합니다.
         */
        Long lastMessageId =
                lastMessage == null ||
                        lastMessage.getMessageId() == null
                        ? 0L
                        : lastMessage.getMessageId();

        return markAsRead(
                projectId,
                normalizedRoomId,
                userNum,
                lastMessageId
        );
    }


    /*
     * 특정 메시지 번호까지 읽음 처리
     *
     * 나중에 스크롤 위치를 기준으로 읽음 처리할 때도
     * 사용할 수 있습니다.
     */
    @Transactional
    public ChatRead markAsRead(
            Long projectId,
            String roomId,
            Long userNum,
            Long messageId
    ) {
        validateRequest(
                projectId,
                roomId,
                userNum
        );

        if (messageId == null) {
            throw new IllegalArgumentException(
                    "읽음 처리할 메시지 번호가 없습니다."
            );
        }

        if (messageId < 0) {
            throw new IllegalArgumentException(
                    "올바르지 않은 메시지 번호입니다."
            );
        }

        String normalizedRoomId =
                roomId.trim();

        ChatReadEntity existingEntity =
                chatReadJpaRepository
                        .findByRoomIdAndUserNum(
                                normalizedRoomId,
                                userNum
                        )
                        .orElse(null);

        /*
         * 이미 더 최신 메시지까지 읽은 상태라면
         * 과거 메시지 번호로 되돌리지 않습니다.
         */
        if (
                existingEntity != null &&
                        existingEntity.getLastReadMessageId() != null &&
                        existingEntity.getLastReadMessageId() >= messageId
        ) {
            return toDto(
                    existingEntity
            );
        }

        ChatReadEntity readEntity;

        if (existingEntity == null) {
            /*
             * 이 방에 대한 읽음 정보가 처음이면
             * 새 Entity를 생성합니다.
             */
            readEntity =
                    ChatReadEntity.builder()
                            .projectId(
                                    projectId
                            )
                            .roomId(
                                    normalizedRoomId
                            )
                            .userNum(
                                    userNum
                            )
                            .lastReadMessageId(
                                    messageId
                            )
                            .readAt(
                                    OffsetDateTime.now()
                            )
                            .build();

        } else {
            /*
             * 기존 읽음 정보가 있으면
             * 마지막 읽은 메시지 번호를 갱신합니다.
             */
            readEntity =
                    existingEntity;

            readEntity.setProjectId(
                    projectId
            );

            readEntity.setRoomId(
                    normalizedRoomId
            );

            readEntity.setUserNum(
                    userNum
            );

            readEntity.setLastReadMessageId(
                    messageId
            );

            readEntity.setReadAt(
                    OffsetDateTime.now()
            );
        }

        ChatReadEntity savedEntity =
                chatReadJpaRepository.save(
                        readEntity
                );

        return toDto(
                savedEntity
        );
    }


    /*
     * 특정 사용자의 특정 채팅방 읽음 정보 조회
     */
    public ChatRead getReadInfo(
            String roomId,
            Long userNum
    ) {
        if (
                roomId == null ||
                        roomId.isBlank() ||
                        userNum == null
        ) {
            return null;
        }

        return chatReadJpaRepository
                .findByRoomIdAndUserNum(
                        roomId.trim(),
                        userNum
                )
                .map(this::toDto)
                .orElse(null);
    }


    /*
     * 사용자가 마지막으로 읽은 메시지 번호 조회
     */
    public Long getLastReadMessageId(
            String roomId,
            Long userNum
    ) {
        ChatRead readInfo =
                getReadInfo(
                        roomId,
                        userNum
                );

        if (
                readInfo == null ||
                        readInfo.getLastReadMessageId() == null
        ) {
            return 0L;
        }

        return readInfo
                .getLastReadMessageId();
    }


    /*
     * 특정 채팅방의 안 읽은 메시지 수 계산
     *
     * 본인이 작성한 메시지는 제외합니다.
     */
    public int getUnreadCount(
            String roomId,
            Long userNum
    ) {
        if (
                roomId == null ||
                        roomId.isBlank() ||
                        userNum == null
        ) {
            return 0;
        }

        String normalizedRoomId =
                roomId.trim();

        Long lastReadMessageId =
                getLastReadMessageId(
                        normalizedRoomId,
                        userNum
                );

        long unreadCount =
                chatService
                        .getMessages(
                                normalizedRoomId
                        )
                        .stream()

                        /*
                         * 메시지 번호가 없는 잘못된 데이터 제외
                         */
                        .filter(message ->
                                message.getMessageId() != null
                        )

                        /*
                         * 마지막으로 읽은 메시지보다
                         * 뒤에 작성된 메시지만 계산
                         */
                        .filter(message ->
                                message.getMessageId() >
                                        lastReadMessageId
                        )

                        /*
                         * 작성자 번호가 없는 데이터 제외
                         */
                        .filter(message ->
                                message.getSenderNum() != null
                        )

                        /*
                         * 본인이 보낸 메시지는
                         * 안 읽은 메시지에 포함하지 않음
                         */
                        .filter(message ->
                                !userNum.equals(
                                        message.getSenderNum()
                                )
                        )
                        .count();

        if (
                unreadCount >
                        Integer.MAX_VALUE
        ) {
            return Integer.MAX_VALUE;
        }

        return (int) unreadCount;
    }


    /*
     * 프로젝트 단체 채팅방의 안 읽은 메시지 수
     */
    public int getProjectUnreadCount(
            Long projectId,
            Long userNum
    ) {
        if (
                projectId == null ||
                        userNum == null
        ) {
            return 0;
        }

        String groupRoomId =
                createGroupRoomId(
                        projectId
                );

        return getUnreadCount(
                groupRoomId,
                userNum
        );
    }


    /*
     * 특정 메시지를 현재 사용자가 읽었는지 확인
     */
    public boolean isRead(
            String roomId,
            Long userNum,
            Long messageId
    ) {
        if (
                roomId == null ||
                        roomId.isBlank() ||
                        userNum == null ||
                        messageId == null
        ) {
            return false;
        }

        Long lastReadMessageId =
                getLastReadMessageId(
                        roomId,
                        userNum
                );

        return lastReadMessageId >=
                messageId;
    }


    /*
     * 개인 메시지를 상대방이 읽었는지 확인
     */
    public boolean isMessageReadByUser(
            ChatMessage message,
            Long receiverUserNum
    ) {
        if (
                message == null ||
                        message.getMessageId() == null ||
                        message.getRoomId() == null ||
                        receiverUserNum == null
        ) {
            return false;
        }

        return isRead(
                message.getRoomId(),
                receiverUserNum,
                message.getMessageId()
        );
    }


    /*
     * 특정 사용자의 특정 방 읽음 정보 삭제
     *
     * 개발 테스트용입니다.
     */
    @Transactional
    public void clearReadInfo(
            String roomId,
            Long userNum
    ) {
        if (
                roomId == null ||
                        roomId.isBlank() ||
                        userNum == null
        ) {
            return;
        }

        chatReadJpaRepository
                .findByRoomIdAndUserNum(
                        roomId.trim(),
                        userNum
                )
                .ifPresent(
                        chatReadJpaRepository::delete
                );
    }


    /*
     * 전체 읽음 정보 삭제
     *
     * 개발 테스트용이며 실제 운영 화면에서는
     * 사용하지 않습니다.
     */
    @Transactional
    public void clearAll() {
        chatReadJpaRepository
                .deleteAllInBatch();
    }


    /*
     * 프로젝트 단체 채팅방 ID 생성
     */
    private String createGroupRoomId(
            Long projectId
    ) {
        return "project_" +
                projectId +
                "_group";
    }


    /*
     * Entity를 DTO로 변환
     */
    private ChatRead toDto(
            ChatReadEntity entity
    ) {
        if (entity == null) {
            return null;
        }

        return ChatRead.builder()
                .readId(
                        entity.getReadId()
                )
                .projectId(
                        entity.getProjectId()
                )
                .roomId(
                        entity.getRoomId()
                )
                .userNum(
                        entity.getUserNum()
                )
                .lastReadMessageId(
                        entity.getLastReadMessageId()
                )
                .readAt(
                        entity.getReadAt()
                )
                .build();
    }


    /*
     * 읽음 처리 요청 검증
     */
    private void validateRequest(
            Long projectId,
            String roomId,
            Long userNum
    ) {
        if (projectId == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        if (
                roomId == null ||
                        roomId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "채팅방 번호가 없습니다."
            );
        }

        if (userNum == null) {
            throw new IllegalArgumentException(
                    "사용자 번호가 없습니다."
            );
        }
    }
}