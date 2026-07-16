package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    private String roomId;
    private String roomName;
    private String roomType; // GROUP, PRIVATE

}