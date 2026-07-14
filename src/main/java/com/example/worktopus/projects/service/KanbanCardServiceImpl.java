package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.request.KanbanCardCreateRequest;
import com.example.worktopus.projects.dto.request.KanbanCardStatusUpdateRequest;
import com.example.worktopus.projects.dto.request.KanbanCardUpdateRequest;
import com.example.worktopus.projects.dto.response.KanbanCardResponse;
import com.example.worktopus.projects.entity.KanbanCard;
import com.example.worktopus.projects.entity.KanbanStatus;
import com.example.worktopus.projects.repository.KanbanCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class KanbanCardServiceImpl implements KanbanCardService {

    private final KanbanCardRepository kanbanCardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<KanbanCardResponse> findProjectCards(Long projectId) {
        return kanbanCardRepository
                .findByProjectIdAndDeletedYnOrderByCreatedAtAsc(projectId, "N")
                .stream()
                .map(KanbanCardResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectCards(Long projectId) {
        return kanbanCardRepository.countByProjectIdAndDeletedYn(projectId, "N");
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectCardsByStatus(Long projectId, KanbanStatus status) {
        return kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(projectId, status, "N");
    }

    @Override
    public KanbanCardResponse create(Long projectId, KanbanCardCreateRequest request) {
        KanbanCard card = new KanbanCard(
                projectId,
                request.title(),
                request.assignee(),
                request.dueDate(),
                request.priority(),
                request.description()
        );

        return KanbanCardResponse.from(kanbanCardRepository.save(card));
    }

    @Override
    public KanbanCardResponse update(Long projectId, Long cardId, KanbanCardUpdateRequest request) {
        KanbanCard card = getCard(projectId, cardId);
        card.update(
                request.title(),
                request.assignee(),
                request.dueDate(),
                request.priority(),
                request.description()
        );

        return KanbanCardResponse.from(card);
    }

    @Override
    public KanbanCardResponse updateStatus(Long projectId, Long cardId, KanbanCardStatusUpdateRequest request) {
        KanbanCard card = getCard(projectId, cardId);
        card.updateStatus(request.status());

        return KanbanCardResponse.from(card);
    }

    @Override
    public void delete(Long projectId, Long cardId) {
        KanbanCard card = getCard(projectId, cardId);
        card.delete();
    }

    private KanbanCard getCard(Long projectId, Long cardId) {
        return kanbanCardRepository
                .findByIdAndProjectIdAndDeletedYn(cardId, projectId, "N")
                .orElseThrow(() -> new IllegalArgumentException("칸반 카드를 찾을 수 없습니다."));
    }
}
