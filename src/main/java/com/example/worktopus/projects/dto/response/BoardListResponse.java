package com.example.worktopus.projects.dto.response;

import com.example.worktopus.projects.entity.Board;

import java.time.LocalDateTime;
public record BoardListResponse(
        Long id,
        Long projectId,
        String title,
        String content,
        String writerName,
        Long viewCount,
        boolean notice,
        String category,
        LocalDateTime createdAt
) {

    public static BoardListResponse from(Board board) {
        return new BoardListResponse(
                board.getId(),
                board.getProjectId(),
                board.getTitle(),
                board.getContent(),
                board.getWriterName(),
                board.getViewCount(),
                "Y".equals(board.getNoticeYn()),
                board.getCategory(),
                board.getCreatedAt()
        );
    }
}