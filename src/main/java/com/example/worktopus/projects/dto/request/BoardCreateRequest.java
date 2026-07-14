package com.example.worktopus.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record BoardCreateRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        String content,

        //@NotBlank
       // @Size(max = 100)
        //String writerName,

        @NotBlank
        String category,

        boolean notice,

        List<MultipartFile> files
) {
}
