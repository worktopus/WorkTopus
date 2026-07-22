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

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class ProjectFileController {

    private final ProjectFileService projectFileService;
    private final ProjectBoardAccessService projectBoardAccessService;

    @GetMapping
    public ModelAndView files(@PathVariable Long projectId,
                              Authentication authentication) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        List<ProjectFileResponse> files = projectFileService.findProjectFiles(projectId);

        ModelAndView mav = new ModelAndView("projects/file");
        mav.addObject("projectId", projectId);
        mav.addObject("files", files);

        return mav;
    }
}
