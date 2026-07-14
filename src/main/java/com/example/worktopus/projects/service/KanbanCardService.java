package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.request.KanbanCardCreateRequest;
import com.example.worktopus.projects.dto.request.KanbanCardStatusUpdateRequest;
import com.example.worktopus.projects.dto.request.KanbanCardUpdateRequest;
import com.example.worktopus.projects.dto.response.KanbanCardResponse;
import com.example.worktopus.projects.entity.KanbanStatus;

import java.util.List;

public interface KanbanCardService {

    List<KanbanCardResponse> findProjectCards(Long projectId);

    long countProjectCards(Long projectId);

    long countProjectCardsByStatus(Long projectId, KanbanStatus status);

    KanbanCardResponse create(Long projectId, KanbanCardCreateRequest request);

    KanbanCardResponse update(Long projectId, Long cardId, KanbanCardUpdateRequest request);

    KanbanCardResponse updateStatus(Long projectId, Long cardId, KanbanCardStatusUpdateRequest request);

    void delete(Long projectId, Long cardId);
}
