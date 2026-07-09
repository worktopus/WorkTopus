package com.example.springedu2.chat.service;

import com.example.springedu2.chat.dto.ProjectMember;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectMemberService {

    private final List<ProjectMember> members = new ArrayList<>();

    public ProjectMemberService(){
        members.add(new ProjectMember(1L,"신승민"));
        members.add(new ProjectMember(2L,"김철수"));
        members.add(new ProjectMember(3L,"홍길동"));
        members.add(new ProjectMember(4L,"이영희"));
    }

    public List<ProjectMember> getMembers(Long projectId){
        // 현재는 테스트용
        return members;
       /* SELECT *
          FROM project_member
          WHERE project_id = ?*/
    }
}