package com.example.springedu2.chat.repository;

import com.example.springedu2.chat.dto.ChatMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ChatRepository {

    private final List<ChatMessage> messages = new ArrayList<>();

    // 저장
    public void save(ChatMessage message) {
        messages.add(message);
    }

    // 전체 조회
    public List<ChatMessage> findAll() {
        return messages;
    }

    // room별 조회
    public List<ChatMessage> findByRoom(String roomId) {

        return messages.stream()
                .filter(message -> roomId.equals(message.getRoomId()))
                .toList();

    }

}