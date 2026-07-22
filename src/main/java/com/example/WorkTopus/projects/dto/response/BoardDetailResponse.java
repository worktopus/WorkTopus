package com.example.WorkTopus.projects.dto.response;

import com.example.WorkTopus.projects.entity.Board;

import java.time.LocalDateTime;
import java.util.List;

public record BoardDetailResponse(
        Long id,
        Long projectId,
        String title,
        String content,
        String category,
        String tag,
        String writerName,
        Long viewCount,
        boolean notice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<FileResponse> files,
        String writerAssignedRole

) {

    public static BoardDetailResponse from(
            Board board,
            List<FileResponse> files,
            String writerAssignedRole
    ) {
        return new BoardDetailResponse(
                board.getId(),
                board.getProjectId(),
                board.getTitle(),
                board.getContent(),
                board.getCategory(),
                board.getTag(),
                board.getWriterName(),
                board.getViewCount(),
                "Y".equals(board.getNoticeYn()),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                files,
                writerAssignedRole
        );
    }
}