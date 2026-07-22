package com.example.WorkTopus.projects.dto.request;

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

        @Size(max = 200)
        String tag,

        List<Long> deleteFileIds,

        List<MultipartFile> files
) {

    public BoardUpdateRequest withTag(String tag) {
        return new BoardUpdateRequest(
                title,
                content,
                category,
                tag != null ? tag : this.tag,
                deleteFileIds,
                files
        );
    }
}
