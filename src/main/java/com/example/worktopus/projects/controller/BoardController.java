package com.example.worktopus.projects.controller;

import com.example.worktopus.common.dto.PageResponse;
import com.example.worktopus.projects.dto.request.BoardCreateRequest;
import com.example.worktopus.projects.dto.request.BoardUpdateRequest;
import com.example.worktopus.projects.dto.response.BoardDetailModalResponse;
import com.example.worktopus.projects.dto.response.BoardDetailResponse;
import com.example.worktopus.projects.dto.response.BoardListResponse;
import com.example.worktopus.projects.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards")
public class BoardController {

    private final BoardService boardService;

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


    @GetMapping("/modal-list")
    public ModelAndView list1(
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

        ModelAndView mav = new ModelAndView("projects/board-list1");
        mav.addObject("projectId", projectId);
        mav.addObject("boards", boards);
        mav.addObject("keyword", keyword);

        return mav;
    }

    // 모달형 게시물 리스트
    @GetMapping("/{boardId}/modal")
    @ResponseBody
    public BoardDetailModalResponse modal(
            @PathVariable Long projectId,
            @PathVariable Long boardId
    ) {
        return boardService.getModal(projectId, boardId);
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
            @PathVariable Long boardId
    ) {
        BoardDetailResponse board = boardService.findDetail(projectId, boardId);

        ModelAndView mav = new ModelAndView("projects/board-detail");
        mav.addObject("projectId", projectId);
        mav.addObject("board", board);

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
