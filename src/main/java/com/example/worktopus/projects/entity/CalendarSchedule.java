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
@Table(name = "PROJECT_CALENDAR_SCHEDULE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CALENDAR_SCHEDULE_ID")
    private Long id;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 20)
    private CalendarScheduleType type = CalendarScheduleType.MEETING;

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

    public CalendarSchedule(Long projectId, String title, LocalDate startDate,
                            LocalDate endDate, String description, CalendarScheduleType type) {
        this.projectId = projectId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.type = type;
    }

    public void update(String title, LocalDate startDate, LocalDate endDate,
                       String description, CalendarScheduleType type) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.type = type;
    }

    public void delete() {
        this.deletedYn = "Y";
        this.deletedAt = LocalDateTime.now();
    }
}
