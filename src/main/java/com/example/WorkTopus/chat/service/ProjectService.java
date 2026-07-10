package com.example.WorkTopus.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ChatRoomService chatRoomService;

    public void createProject(Long projectId, String projectName){

        // 프로젝트 생성(현재는 생략)

        // 기본 단체채팅 생성
        chatRoomService.createProjectGroupRoom(projectId, projectName);

    }

}