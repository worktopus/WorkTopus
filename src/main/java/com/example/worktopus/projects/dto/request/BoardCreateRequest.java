package com.example.worktopus.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoardCreateRequest(

        @NotNull
        Long projectId,

        @NotBlank
        @Size(max = 200)
        String title,

        String content,

        //@NotBlank
       // @Size(max = 100)
        //String writerName,

        @NotBlank
        String category,

        boolean notice
) {
}