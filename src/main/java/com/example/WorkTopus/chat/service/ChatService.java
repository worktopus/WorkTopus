package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_MESSAGE_LENGTH = 2000;

    /*
     * 현재는 메모리 저장 방식이므로
     * 임시 메시지 번호를 생성합니다.
     *
     * DB 연결 후에는 DB 시퀀스 또는 IDENTITY가 담당합니다.
     */
    private final AtomicLong messageSequence =
            new AtomicLong(0);

    private final ChatRepository chatRepository;


    /*
     * 채팅 메시지 저장
     */
    public ChatMessage save(ChatMessage message) {

        validateMessage(message);

        /*
         * 메시지 앞뒤 공백 제거
         */
        message.setMessage(
                message.getMessage().trim()
        );

        /*
         * 메시지 유형 기본값
         */
        if (
                message.getType() == null ||
                        message.getType().isBlank()
        ) {
            message.setType("TALK");

        } else {
            message.setType(
                    message.getType()
                            .trim()
                            .toUpperCase()
            );
        }

        /*
         * Controller 이외의 위치에서 저장해도
         * 작성 시간이 없으면 서버 시간이 들어갑니다.
         */
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(
                    OffsetDateTime.now()
            );
        }

        /*
         * 현재 메모리 저장 단계의 임시 PK입니다.
         */
        if (message.getMessageId() == null) {
            message.setMessageId(
                    messageSequence.incrementAndGet()
            );
        }

        chatRepository.save(message);

        return message;
    }


    /*
     * 특정 채팅방의 이전 메시지 조회
     *
     * 작성 시간 순서로 반환합니다.
     */
    public List<ChatMessage> getMessages(
            String roomId
    ) {
        validateRoomId(roomId);

        return chatRepository
                .findByRoom(roomId.trim())
                .stream()
                .sorted(messageComparator())
                .toList();
    }


    /*
     * 프로젝트 전체 채팅 조회
     *
     * 이후 AI 회의 요약과 회의록 생성에서 사용합니다.
     * 단체 채팅과 개인 채팅이 모두 포함됩니다.
     */
    public List<ChatMessage> getProjectMessages(
            Long projectId
    ) {
        if (projectId == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        return chatRepository
                .findAll()
                .stream()
                .filter(message ->
                        projectId.equals(
                                message.getProjectId()
                        )
                )
                .sorted(messageComparator())
                .toList();
    }


    /*
     * 프로젝트 단체 채팅만 조회
     *
     * AI 회의 요약은 기본적으로
     * 단체 채팅 내용을 사용합니다.
     */
    public List<ChatMessage> getProjectGroupMessages(
            Long projectId
    ) {
        if (projectId == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        String groupRoomId =
                "project_" +
                        projectId +
                        "_group";

        return getMessages(groupRoomId);
    }


    /*
     * 채팅방의 마지막 메시지 조회
     *
     * 프로젝트 목록의 마지막 메시지 출력에 사용합니다.
     */
    public ChatMessage getLastMessage(
            String roomId
    ) {
        List<ChatMessage> messages =
                getMessages(roomId);

        if (messages.isEmpty()) {
            return null;
        }

        return messages.get(
                messages.size() - 1
        );
    }


    /*
     * 메시지 저장 전 검증
     */
    private void validateMessage(
            ChatMessage message
    ) {
        if (message == null) {
            throw new IllegalArgumentException(
                    "메시지 정보가 없습니다."
            );
        }

        if (message.getProjectId() == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        validateRoomId(
                message.getRoomId()
        );

        if (message.getSenderNum() == null) {
            throw new IllegalArgumentException(
                    "메시지 발신자 번호가 없습니다."
            );
        }

        if (
                message.getSenderName() == null ||
                        message.getSenderName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "메시지 발신자 이름이 없습니다."
            );
        }

        if (
                message.getMessage() == null ||
                        message.getMessage().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "메시지 내용을 입력하세요."
            );
        }

        if (
                message.getMessage()
                        .trim()
                        .length() >
                        MAX_MESSAGE_LENGTH
        ) {
            throw new IllegalArgumentException(
                    "메시지는 2000자 이하로 입력하세요."
            );
        }
    }


    /*
     * 채팅방 ID 검증
     */
    private void validateRoomId(
            String roomId
    ) {
        if (
                roomId == null ||
                        roomId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "채팅방 번호가 없습니다."
            );
        }
    }


    /*
     * 메시지 정렬 기준
     *
     * 1. 작성 시간
     * 2. 메시지 번호
     */
    private Comparator<ChatMessage> messageComparator() {
        return Comparator
                .comparing(
                        ChatMessage::getCreatedAt,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
                .thenComparing(
                        ChatMessage::getMessageId,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                );
    }
}