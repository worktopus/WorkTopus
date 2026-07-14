package com.example.worktopus.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {

    private Long id;
    private String originalName;
    private String storedName;
    private String fileUrl;
}