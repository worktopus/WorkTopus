package com.example.WorkTopus.dto;

import com.example.WorkTopus.projects.entity.BoardComment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class Comment {
    private Long id;           // 댓글 ID
    private String content;    // 댓글 내용
    private Long boardId;      // 게시글 ID (bId)
    private String boardTitle; // 게시글 제목
    private Long projectId;    // 프로젝트 ID (pId)
    private String createdAt;  // 작성일자

    // 💡 BoardComment 엔티티를 받아서 DTO 생성
    public Comment(BoardComment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();

        // BoardComment -> Board 참조
        if (comment.getBoard() != null) {
            this.boardId = comment.getBoard().getId();
            this.boardTitle = comment.getBoard().getTitle();
            this.projectId = comment.getBoard().getProjectId(); // 👈 Board에서 바로 꺼냄!
        }

        if (comment.getCreatedAt() != null) {
            this.createdAt = comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}