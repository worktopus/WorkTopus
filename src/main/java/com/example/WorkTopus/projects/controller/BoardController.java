package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.common.dto.PageResponse;
import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
<<<<<<< HEAD
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import com.example.WorkTopus.projects.dto.response.CommentResponse;
import com.example.WorkTopus.projects.service.BoardService;
import com.example.WorkTopus.projects.service.CommentService;
import com.example.WorkTopus.projects.service.ProjectBoardAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
=======
import com.example.WorkTopus.projects.dto.response.BoardDetailModalResponse;
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import com.example.WorkTopus.projects.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
>>>>>>> origin/feature/admin

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards")
public class BoardController {

    private final BoardService boardService;
<<<<<<< HEAD
    private final CommentService commentService;
    private final ProjectBoardAccessService projectBoardAccessService;

    // 게시글 목록
    @GetMapping
    public ModelAndView list(
            @PathVariable Long projectId,
            @PageableDefault(size = 10)
            Pageable pageable,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        Page<BoardListResponse> boardPage =
                boardService.findBoards(projectId, pageable);

        PageResponse<BoardListResponse> boards =
                PageResponse.from(boardPage);

        ModelAndView mav =
                new ModelAndView("projects/board-list");

        mav.addObject("projectId", projectId);
        mav.addObject("boards", boards);
        mav.addObject(
                "latestNotice",
                boardService.getLatestNotice(projectId).orElse(null)
        );

        return mav;
    }

    // 게시글 검색
    @GetMapping("/search")
    @ResponseBody
    public List<BoardListResponse> search(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Pageable pageable = PageRequest.of(0, 1000);

        return boardService.searchBoards(
                projectId,
                keyword,
                category,
                sort,
                pageable
        ).getContent();
    }


    // 게시글 작성
    @GetMapping("/write")
    public ModelAndView writeForm(@PathVariable Long projectId,
                                  Authentication authentication) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );
=======

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
/*
    // 모달형 게시물 리스트
    @GetMapping
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
*/
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
>>>>>>> origin/feature/admin
        ModelAndView mav = new ModelAndView("projects/board-write");
        mav.addObject("projectId", projectId);

        return mav;
    }

    @PostMapping
    public ModelAndView create(
            @PathVariable Long projectId,
<<<<<<< HEAD
            @RequestParam(required = false) String tag,
            @Valid @ModelAttribute BoardCreateRequest request,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        request = request.withTag(tag);

        Long boardId = boardService.create(
                projectId,
                request,
                authentication.getName()
        );

=======
            @Valid @ModelAttribute BoardCreateRequest request
    ) {
        Long boardId = boardService.create(request);
>>>>>>> origin/feature/admin

        return new ModelAndView(
                "redirect:/projects/" + projectId + "/boards/" + boardId
        );
    }

<<<<<<< HEAD
    // 게시글 조회
    @GetMapping("/{boardId}")
    public ModelAndView detail(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        BoardDetailResponse board =
                boardService.findDetail(projectId, boardId);

        boolean isWriter =
                boardService.isWriter(
                        projectId,
                        boardId,
                        authentication.getName()
                );

        boolean canDelete =
                boardService.canDelete(
                        projectId,
                        boardId,
                        authentication.getName()
                );

        List<CommentResponse> comments =
                commentService.findAll(
                        boardId,
                        authentication.getName()
                );

        ModelAndView mav =
                new ModelAndView("projects/board-detail");

        mav.addObject("projectId", projectId);
        mav.addObject("board", board);
        mav.addObject("comments", comments);
        mav.addObject("commentCount", comments.size());
        mav.addObject("isWriter", isWriter);
        mav.addObject("canDelete", canDelete);
=======
    @GetMapping("/{boardId}")
    public ModelAndView detail(
            @PathVariable Long projectId,
            @PathVariable Long boardId
    ) {
        BoardDetailResponse board = boardService.findDetail(projectId, boardId);

        ModelAndView mav = new ModelAndView("projects/board-detail");
        mav.addObject("projectId", projectId);
        mav.addObject("board", board);
>>>>>>> origin/feature/admin

        return mav;
    }

<<<<<<< HEAD
    // 게시글 수정
    @GetMapping("/{boardId}/edit")
    public ModelAndView editForm(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        try {
            BoardDetailResponse board =
                    boardService.findEditableBoard(
                            projectId,
                            boardId,
                            authentication.getName()
                    );

            ModelAndView mav =
                    new ModelAndView("projects/board-edit");

            mav.addObject("projectId", projectId);
            mav.addObject("board", board);

            return mav;

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return new ModelAndView(
                    "redirect:/projects/"
                            + projectId
                            + "/boards/"
                            + boardId
            );
        }
=======
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
>>>>>>> origin/feature/admin
    }

    @PostMapping("/{boardId}/edit")
    public ModelAndView update(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
<<<<<<< HEAD
            @RequestParam(required = false) String tag,
            @Valid @ModelAttribute BoardUpdateRequest request,
            Authentication authentication

    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        request = request.withTag(tag);
        boardService.update(projectId, boardId, request, authentication.getName());
=======
            @Valid @ModelAttribute BoardUpdateRequest request
    ) {
        boardService.update(projectId, boardId, request);
>>>>>>> origin/feature/admin

        return new ModelAndView(
                "redirect:/projects/" + projectId + "/boards/" + boardId
        );
    }

<<<<<<< HEAD
    // 게시글 삭제
    @PostMapping("/{boardId}/delete")
    public ModelAndView delete(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        boardService.delete(
                projectId,
                boardId,
                authentication.getName()
        );
=======
    @PostMapping("/{boardId}/delete")
    public ModelAndView delete(
            @PathVariable Long projectId,
            @PathVariable Long boardId
    ) {
        boardService.delete(projectId, boardId);
>>>>>>> origin/feature/admin

        return new ModelAndView(
                "redirect:/projects/" + projectId + "/boards"
        );
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/feature/admin
