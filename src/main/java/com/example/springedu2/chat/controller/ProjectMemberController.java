package com.example.springedu2.chat.controller;

import com.example.springedu2.chat.dto.ProjectMember;
import com.example.springedu2.chat.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService memberService;

    @GetMapping("/{projectId}")
    public List<ProjectMember> list(
            @PathVariable Long projectId){
        return memberService.getMembers(projectId);

    }

}