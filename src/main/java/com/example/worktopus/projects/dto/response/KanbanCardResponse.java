package com.example.worktopus.projects.dto.response;

import com.example.worktopus.projects.entity.KanbanCard;
import com.example.worktopus.projects.entity.KanbanPriority;
import com.example.worktopus.projects.entity.KanbanStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record KanbanCardResponse(
        Long id,
        Long projectId,
        String title,
        String assignee,
        LocalDate dueDate,
        KanbanPriority priority,
        KanbanStatus status,
        String description
) {

    private static final DateTimeFormatter DUE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("M월 d일");

    public static KanbanCardResponse from(KanbanCard card) {
        return new KanbanCardResponse(
                card.getId(),
                card.getProjectId(),
                card.getTitle(),
                card.getAssignee(),
                card.getDueDate(),
                card.getPriority(),
                card.getStatus(),
                card.getDescription()
        );
    }

    public String priorityValue() {
        return priority.name().toLowerCase();
    }

    public String priorityLabel() {
        return switch (priority) {
            case HIGH -> "High";
            case MEDIUM -> "Medium";
            case LOW -> "Low";
        };
    }

    public String statusValue() {
        return status.name();
    }

    public String assigneeLabel() {
        if (assignee == null || assignee.isBlank()) {
            return "미정";
        }

        return assignee;
    }

    public String assigneeInitial() {
        String label = assigneeLabel();
        return label.length() <= 2 ? label : label.substring(0, 2);
    }

    public String dueDateLabel() {
        if (dueDate == null) {
            return "마감일 없음";
        }

        return dueDate.format(DUE_DATE_FORMATTER);
    }

    public String descriptionLabel() {
        if (description == null || description.isBlank()) {
            return "설명이 없습니다.";
        }

        return description;
    }
}
