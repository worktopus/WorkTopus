package com.example.WorkTopus.projects.repository;

import com.example.WorkTopus.projects.entity.KanbanCard;
import com.example.WorkTopus.projects.entity.KanbanStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
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

    List<KanbanCard> findByProjectIdAndDeletedYnAndDueDateGreaterThanEqualOrderByDueDateAscCreatedAtAsc(
            Long projectId,
            String deletedYn,
            LocalDate dueDate,
            Pageable pageable
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
