package com.example.WorkTopus.dto;

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
    public Post(Long id, String title, LocalDateTime createdAt, String projectName) {
        this.id = id;
        this.title = title;
        this.projectName = projectName;
        setWriteDateFromLocalDateTime(createdAt);
    }

    public void setWriteDateFromLocalDateTime(LocalDateTime createdAt) {
        if (createdAt != null) {
            this.writeDate = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}