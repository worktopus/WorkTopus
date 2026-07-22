package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.common.dto.PageResponse;
import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import com.example.WorkTopus.projects.dto.response.CommentResponse;
import com.example.WorkTopus.projects.service.BoardService;
import com.example.WorkTopus.projects.service.CommentService;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    @GetMapping
    public ModelAndView list(
            @PathVariable Long projectId,
            @PageableDefault(size = 10)
            Pageable pageable
    ) {
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
            @Valid @ModelAttribute BoardCreateRequest request,
            Authentication authentication
    ) {
        request = request.withTag(tag);

        Long boardId = boardService.create(
                projectId,
                request,
                authentication.getName()
        );


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

        return mav;
    }

    @GetMapping("/{boardId}/edit")
    public ModelAndView editForm(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
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
    }

    @PostMapping("/{boardId}/edit")
    public ModelAndView update(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            @RequestParam(required = false) String tag,
            @Valid @ModelAttribute BoardUpdateRequest request,
            Authentication authentication

    ) {
        request = request.withTag(tag);
        boardService.update(projectId, boardId, request, authentication.getName());

        return new ModelAndView(
                "redirect:/projects/" + projectId + "/boards/" + boardId
        );
    }

    @PostMapping("/{boardId}/delete")
    public ModelAndView delete(
            @PathVariable Long projectId,
            @PathVariable Long boardId,
            Authentication authentication
    ) {
        boardService.delete(
                projectId,
                boardId,
                authentication.getName()
        );

        return new ModelAndView(
                "redirect:/projects/" + projectId + "/boards"
        );
    }
}
