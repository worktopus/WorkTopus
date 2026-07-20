package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.common.dto.PageResponse;
import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.BoardDetailModalResponse;
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import com.example.WorkTopus.projects.dto.response.CommentResponse;
import com.example.WorkTopus.projects.service.BoardService;
import com.example.WorkTopus.projects.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    @GetMapping
    public ModelAndView list(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10)
            Pageable pageable
    ) {
        Page<BoardListResponse> boardPage;

        if (keyword == null || keyword.isBlank()) {
            boardPage = boardService.findBoards(projectId, pageable);
        } else {
            boardPage = boardService.searchBoards(projectId, keyword, pageable);
        }

        PageResponse<BoardListResponse> boards = PageResponse.from(boardPage);

        ModelAndView mav = new ModelAndView("projects/board-list");
        mav.addObject("projectId", projectId);
        mav.addObject("boards", boards);
        mav.addObject("keyword", keyword);

        return mav;
    }


    @GetMapping("/write")
    public ModelAndView writeForm(@PathVariable Long projectId) {
        ModelAndView mav = new ModelAndView("projects/board-write");
        mav.addObject("projectId", projectId);

        return mav;
    }

    @PostMapping
    public ModelAndView create(
            @PathVariable Long projectId,
            @RequestParam(required = false) String tag,
            @Valid @ModelAttribute BoardCreateRequest request
    ) {
        request = request.withTag(tag);
        Long boardId = boardService.create(projectId, request);

        return new ModelAndView(
                "redirect:/projects/" + projectId + "/boards/" + boardId
        );
    }

    @GetMapping("/{boardId}")
    public ModelAndView detail(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            Authentication authentication
    ) {
        BoardDetailResponse board = boardService.findDetail(projectId, boardId);

        List<CommentResponse> comments = commentService.findAll(boardId, authentication.getName());

        ModelAndView mav = new ModelAndView("projects/board-detail");
        mav.addObject("projectId", projectId);
        mav.addObject("board", board);
        mav.addObject("comments", comments);
        mav.addObject("commentCount", comments.size());

        return mav;
    }

    @GetMapping("/{boardId}/edit")
    public ModelAndView editForm(
            @PathVariable Long projectId,
            @PathVariable Long boardId
    ) {
        BoardDetailResponse board = boardService.findDetail(projectId, boardId);

        ModelAndView mav = new ModelAndView("projects/board-edit");
        mav.addObject("projectId", projectId);
        mav.addObject("board", board);

        return mav;
    }

    @PostMapping("/{boardId}/edit")
    public ModelAndView update(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            @RequestParam(required = false) String tag,
            @Valid @ModelAttribute BoardUpdateRequest request
    ) {
        request = request.withTag(tag);
        boardService.update(projectId, boardId, request);

        return new ModelAndView(
                "redirect:/projects/" + projectId + "/boards/" + boardId
        );
    }

    @PostMapping("/{boardId}/delete")
    public ModelAndView delete(
            @PathVariable Long projectId,
            @PathVariable Long boardId
    ) {
        boardService.delete(projectId, boardId);

        return new ModelAndView(
                "redirect:/projects/" + projectId + "/boards"
        );
    }
}
