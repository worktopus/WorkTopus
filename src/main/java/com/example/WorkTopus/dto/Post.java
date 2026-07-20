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
    private String title;
    private String writeDate;

    // 생성자
    public Post(Long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        setWriteDateFromLocalDateTime(createdAt);
    }

    public void setWriteDateFromLocalDateTime(LocalDateTime createdAt) {
        if (createdAt != null) {
            this.writeDate = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}