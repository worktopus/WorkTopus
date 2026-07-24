package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.dto.response.ProjectFileResponse;
import com.example.WorkTopus.projects.service.ProjectBoardAccessService;
import com.example.WorkTopus.projects.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * 프로젝트 파일 목록 화면을 조회하는 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class ProjectFileController {

    private final ProjectFileService projectFileService;
    private final ProjectBoardAccessService projectBoardAccessService;

    @GetMapping
    public ModelAndView files(@PathVariable Long projectId,
                              Authentication authentication) {

        // 프로젝트 멤버 권한 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 프로젝트 파일 목록 조회
        List<ProjectFileResponse> files = projectFileService.findProjectFiles(projectId);

        // 파일 목록 화면에 데이터 전달
        ModelAndView mav = new ModelAndView("projects/file");
        mav.addObject("projectId", projectId);
        mav.addObject("files", files);

        return mav;
    }
}
