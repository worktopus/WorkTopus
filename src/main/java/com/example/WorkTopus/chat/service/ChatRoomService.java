package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatRoom;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatRoomService {

    /*
     * 현재 서버에서 사용 중인 프로젝트 단체채팅방
     *
     * WebSocket 요청이 여러 스레드에서 들어올 수 있으므로
     * ConcurrentHashMap을 사용합니다.
     */
    private final Map<String, ChatRoom> chatRooms =
            new ConcurrentHashMap<>();


    /*
     * 채팅방 한 개 조회
     */
    public ChatRoom getRoom(
            String roomId
    ) {
        if (
                roomId == null ||
                        roomId.isBlank()
        ) {
            return null;
        }

        return chatRooms.get(
                roomId.trim()
        );
    }


    /*
     * 프로젝트 기본 단체채팅방 생성
     *
     * 같은 프로젝트의 단체방이 이미 존재하면
     * 기존 객체를 그대로 반환합니다.
     */
    public ChatRoom createProjectGroupRoom(
            Long projectId,
            String projectName
    ) {
        if (projectId == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        if (
                projectName == null ||
                        projectName.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "프로젝트 이름이 없습니다."
            );
        }

        String roomId =
                "project_"
                        + projectId
                        + "_group";

        return chatRooms.computeIfAbsent(
                roomId,
                key -> new ChatRoom(
                        key,
                        projectName.trim()
                                + " 단체채팅",
                        "GROUP"
                )
        );
    }
}