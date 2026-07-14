package com.example.worktopus.projects.dto.response;

import com.example.worktopus.projects.entity.Board;

import java.time.LocalDateTime;

public record DashboardBoardResponse(
        Long id,
        String title,
        String writerName,
        LocalDateTime createdAt
) {

    public static DashboardBoardResponse from(Board board) {
        return new DashboardBoardResponse(
                board.getId(),
                board.getTitle(),
                board.getWriterName(),
                board.getCreatedAt()
        );
    }
}
