package com.example.WorkTopus.projects.dto.response;

import com.example.WorkTopus.projects.entity.Board;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long boardId,
        String title,
        String writerName,
        LocalDateTime createdAt
) {

    public static NoticeResponse from(Board board) {
        return new NoticeResponse(
                board.getId(),
                board.getTitle(),
                board.getWriterName(),
                board.getCreatedAt()
        );
    }
}