package com.example.WorkTopus.chat.repository;

import com.example.WorkTopus.chat.dto.ChatMessage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class ChatRepository {

    /*
     * 임시 채팅 메시지 저장소
     *
     * ArrayList는 여러 사용자가 동시에 메시지를 저장하거나
     * 조회할 때 문제가 발생할 수 있으므로
     * CopyOnWriteArrayList를 사용합니다.
     *
     * 애플리케이션을 재실행하면 저장된 메시지는 사라집니다.
     * 이후 DB 저장 방식으로 교체합니다.
     */
    private final List<ChatMessage> messages =
            new CopyOnWriteArrayList<>();


    /*
     * 메시지 저장
     */
    public ChatMessage save(
            ChatMessage message
    ) {
        if (message == null) {
            throw new IllegalArgumentException(
                    "저장할 메시지가 없습니다."
            );
        }

        messages.add(message);

        return message;
    }


    /*
     * 전체 메시지 조회
     *
     * 내부 리스트를 그대로 반환하지 않고
     * 수정할 수 없는 복사본을 반환합니다.
     */
    public List<ChatMessage> findAll() {
        return List.copyOf(
                messages
        );
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

        String normalizedRoomId =
                roomId.trim();

        return messages.stream()
                .filter(message ->
                        normalizedRoomId.equals(
                                message.getRoomId()
                        )
                )
                .toList();
    }


    /*
     * 특정 프로젝트의 모든 채팅 조회
     *
     * 이후 AI 요약 기능에서도 사용할 수 있습니다.
     */
    public List<ChatMessage> findByProjectId(
            Long projectId
    ) {
        if (projectId == null) {
            return List.of();
        }

        return messages.stream()
                .filter(message ->
                        projectId.equals(
                                message.getProjectId()
                        )
                )
                .toList();
    }


    /*
     * 특정 채팅방의 마지막 메시지 조회
     */
    public ChatMessage findLastByRoom(
            String roomId
    ) {
        List<ChatMessage> roomMessages =
                findByRoom(roomId);

        if (roomMessages.isEmpty()) {
            return null;
        }

        return roomMessages.get(
                roomMessages.size() - 1
        );
    }


    /*
     * 개발 중 저장된 전체 메시지 초기화
     *
     * 실제 운영 기능에서는 사용하지 않습니다.
     */
    public void clear() {
        messages.clear();
    }


    /*
     * 현재 저장된 메시지 수
     */
    public int count() {
        return messages.size();
    }
}