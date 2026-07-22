package com.example.WorkTopus.dto;

import com.example.WorkTopus.projects.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Post {

    private Long id;
    private Long projectId;
    private String title;
    private String writeDate;
    private String projectName;

    // 생성자
    public Post(Board board, String projectName, Long projectId) {
        this.id = board.getId();
        this.projectId = projectId;
        this.title = board.getTitle();
        this.projectName = projectName;
        setWriteDateFromLocalDateTime(board.getCreatedAt());
    }

    public void setWriteDateFromLocalDateTime(LocalDateTime createdAt) {
        if (createdAt != null) {
            this.writeDate = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}