package com.example.WorkTopus.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

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

    private List<String> files;
    private List<String> comments;

}