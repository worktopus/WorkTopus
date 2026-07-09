package com.example.springedu2.chat.controller;

import com.example.springedu2.chat.dto.ChatRoom;
import com.example.springedu2.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService roomService;

    @GetMapping
    public Collection<ChatRoom> list(){

        return roomService.getRooms();

    }

    @GetMapping("/project")
    public ChatRoom createProjectRoom() {
        return roomService.createProjectGroupRoom(
                1L,
                "AI Collaboration"
        );
    }

}
