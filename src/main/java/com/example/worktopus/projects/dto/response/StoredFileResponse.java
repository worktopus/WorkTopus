package com.example.worktopus.projects.dto.response;

public record StoredFileResponse(
        String originalName,
        String storedName,
        String fileUrl,
        String fileExtension,
        Long fileSize,
        String contentType
) {
}
