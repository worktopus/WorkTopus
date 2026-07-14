package com.example.worktopus.projects.controller;

import com.example.worktopus.projects.entity.Board;
import com.example.worktopus.projects.entity.BoardFile;
import com.example.worktopus.projects.repository.BoardFileRepository;
import com.example.worktopus.projects.repository.BoardRepository;
import com.example.worktopus.projects.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class BoardFileController {

    private final BoardFileRepository boardFileRepository;
    private final BoardRepository boardRepository;
    private final FileStorageService fileStorageService;

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long projectId,
            @PathVariable Long fileId
    ) {
        BoardFile boardFile = boardFileRepository.findById(fileId)
                .filter(file -> "N".equals(file.getDeletedYn()))
                .orElseThrow(this::fileNotFound);

        Board board = boardRepository.findById(boardFile.getBoardId())
                .filter(foundBoard -> projectId.equals(foundBoard.getProjectId()))
                .filter(foundBoard -> "N".equals(foundBoard.getDeletedYn()))
                .orElseThrow(this::fileNotFound);

        Resource resource;

        try {
            resource = fileStorageService.load(boardFile.getStoredName());
        } catch (RuntimeException e) {
            throw fileNotFound();
        }

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(boardFile.getOriginalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(resolveContentType(boardFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    private MediaType resolveContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private ResponseStatusException fileNotFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "파일을 찾을 수 없습니다."
        );
    }
}
