package com.example.springedu2.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // 나중에 로그인 유저아이디로 고치기
public class ChatMessage {
    private String sender;      // 보낸 사람
    private String message;     // 메시지 내용
    private String roomId;      // 채팅방 ID
    private String type;        // ENTER, TALK, LEAVE
}