package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.dto.request.KanbanCardCreateRequest;
import com.example.WorkTopus.projects.dto.request.KanbanCardStatusUpdateRequest;
import com.example.WorkTopus.projects.dto.request.KanbanCardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.KanbanCardResponse;
import com.example.WorkTopus.projects.entity.KanbanStatus;
import com.example.WorkTopus.projects.service.KanbanCardService;
import com.example.WorkTopus.projects.service.ProjectBoardAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards/kanban")
public class KanbanController {

    private final KanbanCardService kanbanCardService;
    private final ProjectBoardAccessService projectBoardAccessService;

    @GetMapping
    public ModelAndView kanban(@PathVariable Long projectId,
                               Authentication authentication) {

        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        List<KanbanCardResponse> cards = kanbanCardService.findProjectCards(projectId);

        ModelAndView mav = new ModelAndView("projects/kanban");
        mav.addObject("projectId", projectId);
        mav.addObject("todoCards", filterByStatus(cards, KanbanStatus.TODO));
        mav.addObject("inProgressCards", filterByStatus(cards, KanbanStatus.IN_PROGRESS));
        mav.addObject("reviewCards", filterByStatus(cards, KanbanStatus.REVIEW));
        mav.addObject("doneCards", filterByStatus(cards, KanbanStatus.DONE));
        mav.addObject("totalCount", kanbanCardService.countProjectCards(projectId));
        mav.addObject("inProgressCount", kanbanCardService.countProjectCardsByStatus(projectId, KanbanStatus.IN_PROGRESS));
        mav.addObject("doneCount", kanbanCardService.countProjectCardsByStatus(projectId, KanbanStatus.DONE));

        return mav;
    }

    //카드 목록 조회
    @GetMapping("/cards")
    @ResponseBody
    public List<KanbanCardResponse> cards(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        return kanbanCardService.findProjectCards(projectId);
    }

    // 카드 생성
    @PostMapping("/cards")
    @ResponseBody
    public KanbanCardResponse create(
            @PathVariable Long projectId,
            @Valid @RequestBody KanbanCardCreateRequest request,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        return kanbanCardService.create(projectId, request);
    }

    // 카드 수정
    @PutMapping("/cards/{cardId}")
    @ResponseBody
    public KanbanCardResponse update(
            @PathVariable Long projectId,
            @PathVariable Long cardId,
            @Valid @RequestBody KanbanCardUpdateRequest request,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        return kanbanCardService.update(
                projectId,
                cardId,
                request
        );
    }

    // 카드 삭제
    @DeleteMapping("/cards/{cardId}")
    @ResponseBody
    public void delete(
            @PathVariable Long projectId,
            @PathVariable Long cardId,
            Authentication authentication
    ) {
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        kanbanCardService.delete(projectId, cardId);
    }

    // 카드 상태 변경
    @PatchMapping("/cards/{cardId}/status")
    @ResponseBody
    public KanbanCardResponse updateStatus(
            @PathVariable Long projectId,
            @PathVariable Long cardId,
            @Valid @RequestBody KanbanCardStatusUpdateRequest request,
            Authentication authentication
    ) {

        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        return kanbanCardService.updateStatus(
                projectId,
                cardId,
                request
        );
    }

    private List<KanbanCardResponse> filterByStatus(
            List<KanbanCardResponse> cards,
            KanbanStatus status
    ) {
        return cards.stream()
                .filter(card -> card.status() == status)
                .toList();
    }
}
