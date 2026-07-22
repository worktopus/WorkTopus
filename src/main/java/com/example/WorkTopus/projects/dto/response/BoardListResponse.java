package com.example.WorkTopus.projects.dto.response;

import com.example.WorkTopus.projects.entity.Board;
<<<<<<< HEAD
import org.jsoup.Jsoup;
=======
>>>>>>> origin/feature/admin

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
        LocalDateTime createdAt,
        long commentCount
) {

    public static BoardListResponse from(
            Board board,
            Long commentCount
    ) {
        return new BoardListResponse(
                board.getId(),
                board.getProjectId(),
                board.getTitle(),
                board.getContent(),
                board.getWriterName(),
                board.getViewCount(),
                "Y".equals(board.getNoticeYn()),
                board.getCategory(),
                board.getCreatedAt(),
                commentCount
        );
    }

    public String contentPreview() {
        if (content == null || content.isBlank()) {
            return "";
        }

        return Jsoup.parse(content).text();
    }
}