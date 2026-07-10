package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(ChatMessage message) {

        chatService.save(message);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getRoomId(),
                message
        );

    }

    @GetMapping("/chat/history")
    @ResponseBody
    public List<ChatMessage> history() {
        return chatService.getMessages();
    }

    @GetMapping("/chat/history/{roomId}")
    @ResponseBody
    public List<ChatMessage> historyByRoom(@PathVariable String roomId) {
        return chatService.getMessages(roomId);
    }

}