package com.example.WorkTopus.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record BoardCreateRequest(

        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 200)
        String title,

        @NotBlank(message = "내용을 입력해 주세요.")
        String content,

        @NotBlank
        String category,

        @Size(max = 200)
        String tag,

        List<MultipartFile> files
) {

    public BoardCreateRequest withTag(String tag) {
        return new BoardCreateRequest(
                title,
                content,
                category,
                tag != null ? tag : this.tag,
                files
        );
    }
}