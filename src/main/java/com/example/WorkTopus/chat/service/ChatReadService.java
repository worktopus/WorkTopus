package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.dto.ChatRead;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    /*
     * 현재는 DB 연결 전이므로 메모리에 저장합니다.
     *
     * Key 형식:
     * roomId:userNum
     *
     * 예:
     * project_2_group:1
     */
    private final Map<String, ChatRead> readInfoMap =
            new ConcurrentHashMap<>();

    /*
     * 임시 읽음 정보 PK
     *
     * DB 연결 후에는 시퀀스 또는 IDENTITY로 교체합니다.
     */
    private final AtomicLong readSequence =
            new AtomicLong(0);

    private final ChatService chatService;


    /*
     * 현재 채팅방의 마지막 메시지까지 읽음 처리
     */
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

        List<ChatMessage> messages =
                chatService.getMessages(
                        roomId
                );

        Long lastMessageId =
                messages.stream()
                        .map(
                                ChatMessage::getMessageId
                        )
                        .filter(
                                messageId ->
                                        messageId != null
                        )
                        .max(Long::compareTo)
                        .orElse(0L);

        return saveReadInfo(
                projectId,
                roomId,
                userNum,
                lastMessageId
        );
    }


    /*
     * 특정 메시지 번호까지 읽음 처리
     *
     * 나중에 스크롤 기반 읽음 처리에도 사용할 수 있습니다.
     */
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

        ChatRead currentRead =
                getReadInfo(
                        roomId,
                        userNum
                );

        /*
         * 이미 더 최신 메시지까지 읽었다면
         * 과거 메시지 번호로 되돌리지 않습니다.
         */
        if (
                currentRead != null &&
                        currentRead.getLastReadMessageId() != null &&
                        currentRead.getLastReadMessageId() >= messageId
        ) {
            return currentRead;
        }

        return saveReadInfo(
                projectId,
                roomId,
                userNum,
                messageId
        );
    }


    /*
     * 읽음 정보 저장 또는 갱신
     */
    private ChatRead saveReadInfo(
            Long projectId,
            String roomId,
            Long userNum,
            Long lastReadMessageId
    ) {
        String normalizedRoomId =
                roomId.trim();

        String key =
                createReadKey(
                        normalizedRoomId,
                        userNum
                );

        ChatRead existingRead =
                readInfoMap.get(key);

        ChatRead readInfo =
                ChatRead.builder()
                        .readId(
                                existingRead != null
                                        ? existingRead.getReadId()
                                        : readSequence.incrementAndGet()
                        )
                        .projectId(projectId)
                        .roomId(normalizedRoomId)
                        .userNum(userNum)
                        .lastReadMessageId(
                                lastReadMessageId
                        )
                        .readAt(
                                OffsetDateTime.now()
                        )
                        .build();

        readInfoMap.put(
                key,
                readInfo
        );

        return readInfo;
    }


    /*
     * 특정 사용자의 채팅방 읽음 정보 조회
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

        return readInfoMap.get(
                createReadKey(
                        roomId.trim(),
                        userNum
                )
        );
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

        return readInfo.getLastReadMessageId();
    }


    /*
     * 특정 채팅방의 안 읽은 메시지 수
     *
     * 본인이 보낸 메시지는 제외합니다.
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

        Long lastReadMessageId =
                getLastReadMessageId(
                        roomId,
                        userNum
                );

        return (int) chatService
                .getMessages(roomId)
                .stream()
                .filter(message ->
                        message.getMessageId() != null
                )
                .filter(message ->
                        message.getMessageId() >
                                lastReadMessageId
                )
                .filter(message ->
                        message.getSenderNum() != null
                )
                .filter(message ->
                        !userNum.equals(
                                message.getSenderNum()
                        )
                )
                .count();
    }


    /*
     * 프로젝트 단체 채팅의 안 읽은 메시지 수
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
     * 개인 메시지의 상대방 읽음 여부 확인
     *
     * receiverUserNum이 해당 메시지까지 읽었다면 true입니다.
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
     * 읽음 정보 삭제
     *
     * 개발 테스트용입니다.
     */
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

        readInfoMap.remove(
                createReadKey(
                        roomId.trim(),
                        userNum
                )
        );
    }


    /*
     * 전체 임시 읽음 정보 삭제
     *
     * 개발 테스트용입니다.
     */
    public void clearAll() {
        readInfoMap.clear();
    }


    /*
     * 읽음 정보 Key 생성
     */
    private String createReadKey(
            String roomId,
            Long userNum
    ) {
        return roomId +
                ":" +
                userNum;
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