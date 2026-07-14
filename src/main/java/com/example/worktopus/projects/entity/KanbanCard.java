package com.example.worktopus.projects.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECT_KANBAN_CARD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KanbanCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KANBAN_CARD_ID")
    private Long id;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "ASSIGNEE", length = 100)
    private String assignee;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRIORITY", nullable = false, length = 20)
    private KanbanPriority priority = KanbanPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private KanbanStatus status = KanbanStatus.TODO;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "IS_DELETED", nullable = false, length = 1)
    private String deletedYn = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    public KanbanCard(Long projectId, String title, String assignee, LocalDate dueDate,
                      KanbanPriority priority, String description) {
        this.projectId = projectId;
        this.title = title;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.priority = priority;
        this.description = description;
    }

    public void update(String title, String assignee, LocalDate dueDate,
                       KanbanPriority priority, String description) {
        this.title = title;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.priority = priority;
        this.description = description;
    }

    public void updateStatus(KanbanStatus status) {
        this.status = status;
    }

    public void delete() {
        this.deletedYn = "Y";
        this.deletedAt = LocalDateTime.now();
    }
}
