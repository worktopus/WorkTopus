package com.example.worktopus.projects.dto.response;

import com.example.worktopus.projects.entity.Board;

import java.time.LocalDateTime;

public record BoardDetailResponse(
        Long id,
        Long projectId,
        String title,
        String content,
        String writerName,
        Long viewCount,
        boolean notice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static BoardDetailResponse from(Board board) {
        return new BoardDetailResponse(
                board.getId(),
                board.getProjectId(),
                board.getTitle(),
                board.getContent(),
                board.getWriterName(),
                board.getViewCount(),
                "Y".equals(board.getNoticeYn()),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}