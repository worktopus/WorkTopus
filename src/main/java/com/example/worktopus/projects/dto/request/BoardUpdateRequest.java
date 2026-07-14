package com.example.worktopus.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record BoardUpdateRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        String content,

        @NotBlank
        String category,

        boolean notice,

        List<Long> deleteFileIds,

        List<MultipartFile> files
) {
}
