package com.example.WorkTopus.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardUpdateRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        String content,

        @NotBlank
        String category,

        boolean notice
) {
}