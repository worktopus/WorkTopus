package com.example.worktopus.projects.repository;

import com.example.worktopus.projects.entity.CalendarSchedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalendarScheduleRepository extends JpaRepository<CalendarSchedule, Long> {

    List<CalendarSchedule> findByProjectIdAndDeletedYnOrderByStartDateAscCreatedAtAsc(
            Long projectId,
            String deletedYn
    );

    Optional<CalendarSchedule> findByIdAndProjectIdAndDeletedYn(
            Long id,
            Long projectId,
            String deletedYn
    );

    List<CalendarSchedule> findByProjectIdAndDeletedYnAndStartDateGreaterThanEqualOrderByStartDateAscCreatedAtAsc(
            Long projectId,
            String deletedYn,
            LocalDate startDate,
            Pageable pageable
    );
}
