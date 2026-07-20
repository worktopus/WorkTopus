package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.dto.request.CommentCreateRequest;
import com.example.WorkTopus.projects.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public String createComment(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            @Valid @ModelAttribute CommentCreateRequest request,
            Authentication authentication
    ) {
        commentService.create(
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
        commentService.delete(
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

        commentService.update(
                boardId,
                commentId,
                authentication.getName(),
                content
        );

        return "redirect:/projects/" + projectId
                + "/boards/" + boardId;
    }
}