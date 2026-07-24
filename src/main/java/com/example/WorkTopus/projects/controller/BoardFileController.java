package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.entity.Board;
import com.example.WorkTopus.projects.entity.BoardFile;
import com.example.WorkTopus.projects.repository.BoardFileRepository;
import com.example.WorkTopus.projects.repository.BoardRepository;
import com.example.WorkTopus.projects.service.FileStorageService;
import com.example.WorkTopus.projects.service.ProjectBoardAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
/**
 * 게시판 첨부파일 다운로드를 처리하는 컨트롤러.
 * 프로젝트 멤버만 다운로드할 수 있도록 권한을 검증한다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class BoardFileController {

    private final BoardFileRepository boardFileRepository;
    private final BoardRepository boardRepository;
    private final FileStorageService fileStorageService;
    private final ProjectBoardAccessService projectBoardAccessService;

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long projectId,
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        // 프로젝트 멤버 여부 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 삭제되지 않은 첨부파일 조회
        BoardFile boardFile = boardFileRepository.findById(fileId)
                .filter(file -> "N".equals(file.getDeletedYn()))
                .orElseThrow(this::fileNotFound);

        // 해당 파일이 현재 프로젝트의 게시글에 속하는지 검증
        Board board = boardRepository.findById(boardFile.getBoardId())
                .filter(foundBoard -> projectId.equals(foundBoard.getProjectId()))
                .filter(foundBoard -> "N".equals(foundBoard.getDeletedYn()))
                .orElseThrow(this::fileNotFound);

        // 서버에 저장된 실제 파일 로드
        Resource resource;

        try {
            resource = fileStorageService.load(boardFile.getStoredName());
        } catch (RuntimeException e) {
            throw fileNotFound();
        }

        // 다운로드 응답 생성
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(boardFile.getOriginalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(resolveContentType(boardFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    // Content-Type이 없거나 올바르지 않으면 기본 바이너리 타입을 반환
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

    // 파일을 찾을 수 없을 때 공통 404 예외 반환
    private ResponseStatusException fileNotFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "파일을 찾을 수 없습니다."
        );
    }
}
