package com.example.WorkTopus.projects.dto.response;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

public record ProjectFileResponse(
        Long fileId,
        Long boardId,
        String originalName,
        String fileExtension,
        Long fileSize,
        String boardTitle,
        String writerName,
        LocalDateTime createdAt
) {

    public String formattedFileSize() {
        if (fileSize == null || fileSize < 1024) {
            return (fileSize == null ? 0 : fileSize) + " B";
        }

        double size = fileSize;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unitIndex = -1;

        do {
            size /= 1024;
            unitIndex++;
        } while (size >= 1024 && unitIndex < units.length - 1);

        return new DecimalFormat("0.#").format(size) + " " + units[unitIndex];
    }
}
