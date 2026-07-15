package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private static final int MAX_MESSAGE_LENGTH =
            2000;

    private final ChatRepository chatRepository;


    /*
     * 채팅 메시지 저장
     */
    @Transactional
    public ChatMessage save(
            ChatMessage message
    ) {
        validateMessage(
                message
        );

        /*
         * 메시지 앞뒤 공백 제거
         */
        message.setMessage(
                message.getMessage()
                        .trim()
        );

        /*
         * 발신자 이름 앞뒤 공백 제거
         */
        message.setSenderName(
                message.getSenderName()
                        .trim()
        );

        /*
         * 채팅방 ID 앞뒤 공백 제거
         */
        message.setRoomId(
                message.getRoomId()
                        .trim()
        );

        /*
         * 메시지 유형 기본값 설정
         */
        if (
                message.getType() == null ||
                        message.getType().isBlank()
        ) {
            message.setType(
                    "TALK"
            );

        } else {
            message.setType(
                    message.getType()
                            .trim()
                            .toUpperCase()
            );
        }

        /*
         * Controller가 작성 시간을 넣지 않은 경우
         * 서버 시간을 사용합니다.
         */
        if (
                message.getCreatedAt() == null
        ) {
            message.setCreatedAt(
                    OffsetDateTime.now()
            );
        }

        /*
         * 기존 AtomicLong 메시지 번호 생성은 삭제했습니다.
         *
         * Oracle DB가 MESSAGE_ID를 자동 생성하고,
         * 저장된 결과를 다시 ChatMessage로 반환합니다.
         */
        return chatRepository.save(
                message
        );
    }


    /*
     * 특정 채팅방의 이전 메시지 조회
     */
    public List<ChatMessage> getMessages(
            String roomId
    ) {
        validateRoomId(
                roomId
        );

        return chatRepository
                .findByRoom(
                        roomId.trim()
                )
                .stream()
                .sorted(
                        messageComparator()
                )
                .toList();
    }


    /*
     * 특정 프로젝트의 전체 채팅 조회
     *
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
                .findByProjectId(
                        projectId
                )
                .stream()
                .sorted(
                        messageComparator()
                )
                .toList();
    }


    /*
     * 특정 프로젝트의 단체 채팅만 조회
     *
     * AI 회의요약과 회의록 생성에서
     * 기본적으로 사용할 메시지입니다.
     */
    public List<ChatMessage>
    getProjectGroupMessages(
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

        return getMessages(
                groupRoomId
        );
    }


    /*
     * 특정 채팅방의 마지막 메시지 조회
     */
    public ChatMessage getLastMessage(
            String roomId
    ) {
        validateRoomId(
                roomId
        );

        return chatRepository
                .findLastByRoom(
                        roomId.trim()
                );
    }


    /*
     * 현재 저장된 전체 메시지 수
     */
    public int getMessageCount() {
        return chatRepository.count();
    }


    /*
     * 개발 테스트용 전체 메시지 초기화
     */
    @Transactional
    public void clearMessages() {
        chatRepository.clear();
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

        if (
                message.getProjectId() == null
        ) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        validateRoomId(
                message.getRoomId()
        );

        if (
                message.getSenderNum() == null
        ) {
            throw new IllegalArgumentException(
                    "메시지 발신자 번호가 없습니다."
            );
        }

        if (
                message.getSenderName() == null ||
                        message.getSenderName()
                                .isBlank()
        ) {
            throw new IllegalArgumentException(
                    "메시지 발신자 이름이 없습니다."
            );
        }

        if (
                message.getMessage() == null ||
                        message.getMessage()
                                .isBlank()
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
    private Comparator<ChatMessage>
    messageComparator() {
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