package com.example.worktopus.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardDetailModalResponse {

    private Long id;

    private String title;

    private String content;

    private String writerName;

    private Long viewCount;

    private boolean notice;

    private String createdAt;

    private Long commentCount;

    private List<FileResponse> files;

    private List<CommentResponse> comments;

}