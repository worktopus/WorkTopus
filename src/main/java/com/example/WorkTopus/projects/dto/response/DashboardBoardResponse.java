package com.example.WorkTopus.projects.dto.response;

import com.example.WorkTopus.projects.entity.Board;

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
