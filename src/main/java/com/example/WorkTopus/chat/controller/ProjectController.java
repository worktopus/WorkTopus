package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/project/create")
    public String createProject(){

        projectService.createProject(
                2L,
                "WorkTopus"
        );

        return "프로젝트 생성 완료";

    }

}