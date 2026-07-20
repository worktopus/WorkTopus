package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatRoom;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatRoomService {  // 채팅방 관리 클래스 생성
    private final Map<String, ChatRoom> chatRooms = new HashMap<>();

    public ChatRoomService(){
        createRoom(
                "group",
                "프로젝트 단체채팅",
                "GROUP"
        );
    }

    public ChatRoom createRoom(String roomId, String roomName, String roomType){

        ChatRoom room = new ChatRoom(roomId, roomName, roomType);
        chatRooms.put(roomId, room);

        return room;
    }

    public Collection<ChatRoom> getRooms(){
        return chatRooms.values();
    }

    public ChatRoom getRoom(String roomId){
        return chatRooms.get(roomId);
    }

    public ChatRoom createProjectGroupRoom(Long projectId, String projectName) {

        String roomId = "project_" + projectId + "_group";

        ChatRoom room = new ChatRoom(
                roomId,
                projectName + " 단체채팅",
                "GROUP"
        );

        chatRooms.put(roomId, room);

        return room;
    }
}