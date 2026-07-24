package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.dto.request.CommentCreateRequest;
import com.example.WorkTopus.projects.service.CommentService;
import com.example.WorkTopus.projects.service.ProjectBoardAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 댓글 등록, 수정, 삭제 요청을 처리하는 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final ProjectBoardAccessService projectBoardAccessService;

    @PostMapping
    public String createComment(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            @Valid @ModelAttribute CommentCreateRequest request,
            Authentication authentication
    ) {
        // 프로젝트 멤버 권한 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 댓글 등록
        commentService.create(
                projectId,
                boardId,
                authentication.getName(),
                request.getContent()
        );

        return "redirect:/projects/" + projectId
                + "/boards/" + boardId;
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        // 프로젝트 멤버 권한 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 댓글 삭제
        commentService.delete(
                projectId,
                boardId,
                commentId,
                authentication.getName()
        );

        return "redirect:/projects/" + projectId
                + "/boards/" + boardId;
    }

    @PostMapping("/{commentId}/edit")
    public String updateComment(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestParam String content,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 댓글 수정
        commentService.update(
                projectId,
                boardId,
                commentId,
                authentication.getName(),
                content
        );

        return "redirect:/projects/" + projectId
                + "/boards/" + boardId;
    }
}