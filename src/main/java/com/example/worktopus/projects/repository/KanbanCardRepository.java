package com.example.worktopus.projects.repository;

import com.example.worktopus.projects.entity.KanbanCard;
import com.example.worktopus.projects.entity.KanbanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KanbanCardRepository extends JpaRepository<KanbanCard, Long> {

    List<KanbanCard> findByProjectIdAndDeletedYnOrderByCreatedAtAsc(
            Long projectId,
            String deletedYn
    );

    List<KanbanCard> findByProjectIdAndDeletedYnAndDueDateIsNotNullOrderByDueDateAscCreatedAtAsc(
            Long projectId,
            String deletedYn
    );

    long countByProjectIdAndDeletedYn(Long projectId, String deletedYn);

    long countByProjectIdAndStatusAndDeletedYn(
            Long projectId,
            KanbanStatus status,
            String deletedYn
    );

    Optional<KanbanCard> findByIdAndProjectIdAndDeletedYn(
            Long id,
            Long projectId,
            String deletedYn
    );
}
