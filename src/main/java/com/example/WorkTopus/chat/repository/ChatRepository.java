package com.example.WorkTopus.chat.repository;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.entity.ChatMessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRepository {

    /*
     * Spring Data JPA Repository
     *
     * 기존 CopyOnWriteArrayList를 대신해서
     * Oracle CHAT_MESSAGE 테이블을 사용합니다.
     */
    private final ChatMessageJpaRepository
            chatMessageJpaRepository;


    /*
     * 메시지 저장
     */
    @Transactional
    public ChatMessage save(
            ChatMessage message
    ) {
        if (message == null) {
            throw new IllegalArgumentException(
                    "저장할 메시지가 없습니다."
            );
        }

        ChatMessageEntity entity =
                toEntity(
                        message
                );

        ChatMessageEntity savedEntity =
                chatMessageJpaRepository.save(
                        entity
                );

        return toDto(
                savedEntity
        );
    }


    /*
     * 모든 메시지 조회
     *
     * 메시지 번호가 작은 순서,
     * 즉 오래된 메시지부터 반환합니다.
     */
    public List<ChatMessage> findAll() {
        return chatMessageJpaRepository
                .findAll(
                        Sort.by(
                                Sort.Direction.ASC,
                                "messageId"
                        )
                )
                .stream()
                .map(this::toDto)
                .toList();
    }


    /*
     * 특정 채팅방 메시지 조회
     */
    public List<ChatMessage> findByRoom(
            String roomId
    ) {
        if (
                roomId == null ||
                        roomId.isBlank()
        ) {
            return List.of();
        }

        return chatMessageJpaRepository
                .findByRoomIdOrderByMessageIdAsc(
                        roomId.trim()
                )
                .stream()
                .map(this::toDto)
                .toList();
    }


    /*
     * 특정 프로젝트의 전체 메시지 조회
     */
    public List<ChatMessage> findByProjectId(
            Long projectId
    ) {
        if (projectId == null) {
            return List.of();
        }

        return chatMessageJpaRepository
                .findByProjectIdOrderByMessageIdAsc(
                        projectId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }


    /*
     * 특정 채팅방의 마지막 메시지 조회
     */
    public ChatMessage findLastByRoom(
            String roomId
    ) {
        if (
                roomId == null ||
                        roomId.isBlank()
        ) {
            return null;
        }

        return chatMessageJpaRepository
                .findTopByRoomIdOrderByMessageIdDesc(
                        roomId.trim()
                )
                .map(this::toDto)
                .orElse(null);
    }


    /*
     * 테스트용 전체 메시지 삭제
     *
     * 실제 운영 화면에서는 호출하지 않습니다.
     */
    @Transactional
    public void clear() {
        chatMessageJpaRepository
                .deleteAllInBatch();
    }


    /*
     * 저장된 전체 메시지 수
     */
    public int count() {
        long count =
                chatMessageJpaRepository.count();

        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) count;
    }


    /*
     * DTO를 DB Entity로 변환
     */
    private ChatMessageEntity toEntity(
            ChatMessage message
    ) {
        String type =
                message.getType();

        if (
                type == null ||
                        type.isBlank()
        ) {
            type = "TALK";
        }

        return ChatMessageEntity.builder()
                /*
                 * messageId는 넣지 않습니다.
                 *
                 * Oracle DB가 자동으로 생성해야 하므로
                 * 새 메시지는 항상 null 상태로 저장합니다.
                 */
                .projectId(
                        message.getProjectId()
                )
                .roomId(
                        message.getRoomId() == null
                                ? null
                                : message.getRoomId()
                                .trim()
                )
                .senderNum(
                        message.getSenderNum()
                )
                .senderName(
                        message.getSenderName() == null
                                ? null
                                : message.getSenderName()
                                .trim()
                )
                .message(
                        message.getMessage() == null
                                ? null
                                : message.getMessage()
                                .trim()
                )
                .type(
                        type.trim()
                                .toUpperCase()
                )
                .createdAt(
                        message.getCreatedAt()
                )
                .build();
    }


    /*
     * DB Entity를 화면 전송용 DTO로 변환
     */
    private ChatMessage toDto(
            ChatMessageEntity entity
    ) {
        if (entity == null) {
            return null;
        }

        return ChatMessage.builder()
                .messageId(
                        entity.getMessageId()
                )
                .projectId(
                        entity.getProjectId()
                )
                .roomId(
                        entity.getRoomId()
                )
                .senderNum(
                        entity.getSenderNum()
                )
                .senderName(
                        entity.getSenderName()
                )
                .message(
                        entity.getMessage()
                )
                .type(
                        entity.getType()
                )
                .createdAt(
                        entity.getCreatedAt()
                )
                /*
                 * 읽음 여부는 CHAT_MESSAGE 컬럼이 아니라
                 * 읽음 정보에서 별도로 계산합니다.
                 */
                .unreadCount(null)
                .readYn(null)
                .build();
    }
}