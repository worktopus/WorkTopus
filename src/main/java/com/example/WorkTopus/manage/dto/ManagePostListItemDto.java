package com.example.WorkTopus.manage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ManagePostListItemDto {

    private Long boardId;
    private String title;
    private String category;
    private String writerName;
    private Long viewCount;
    private LocalDateTime createdAt;
    private boolean notice;
}
