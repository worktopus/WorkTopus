package com.example.springedu2.chat.service;

import com.example.springedu2.chat.dto.ChatMessage;
import com.example.springedu2.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository repository;

    // 채팅 저장
    public void save(ChatMessage message) {
        repository.save(message);
    }

    // 전체 채팅 조회
    public List<ChatMessage> getMessages() {
        return repository.findAll();
    }

    // 특정 채팅방(room)만 조회
    public List<ChatMessage> getMessages(String roomId) {
        return repository.findByRoom(roomId);
    }

}