package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.request.BoardCreateRequest;
import com.example.worktopus.projects.dto.request.BoardUpdateRequest;
import com.example.worktopus.projects.dto.response.BoardDetailModalResponse;
import com.example.worktopus.projects.dto.response.BoardDetailResponse;
import com.example.worktopus.projects.dto.response.BoardListResponse;
import com.example.worktopus.projects.dto.response.FileResponse;
import com.example.worktopus.projects.dto.response.StoredFileResponse;
import com.example.worktopus.projects.entity.Board;
import com.example.worktopus.projects.entity.BoardFile;
import com.example.worktopus.projects.exception.BoardNotFoundException;
import com.example.worktopus.projects.repository.BoardFileRepository;
import com.example.worktopus.projects.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Long create(Long projectId, BoardCreateRequest request) {
        Board board = new Board(
                projectId,
                request.title(),
                request.content(),
                "관리자",
                request.notice(),
                request.category(),
                request.tag()
        );

        Board savedBoard = boardRepository.save(board);

        saveAttachments(savedBoard.getId(), request.files());

        return savedBoard.getId();
    }

    private void saveAttachments(Long boardId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            StoredFileResponse storedFile = fileStorageService.store(file);
            BoardFile boardFile = new BoardFile(
                    boardId,
                    storedFile.originalName(),
                    storedFile.storedName(),
                    storedFile.fileUrl(),
                    storedFile.fileExtension(),
                    storedFile.fileSize(),
                    storedFile.contentType()
            );

            boardFileRepository.save(boardFile);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoardListResponse> findBoards(Long projectId, Pageable pageable) {
        return boardRepository
                .findByProjectIdAndDeletedYnOrderByNoticeYnDescCreatedAtDesc(
                        projectId,
                        "N",
                        pageable
                )
                .map(BoardListResponse::from);
    }

    @Override
    public BoardDetailResponse findDetail(Long projectId, Long boardId) {
        Board board = getBoard(projectId, boardId);
        board.increaseViewCount();

        List<FileResponse> files = boardFileRepository
                .findByBoardIdAndDeletedYnOrderByCreatedAtAsc(boardId, "N")
                .stream()
                .map(file -> FileResponse.builder()
                        .id(file.getId())
                        .originalName(file.getOriginalName())
                        .storedName(file.getStoredName())
                        .fileUrl(file.getFileUrl())
                        .build())
                .toList();

        return BoardDetailResponse.from(board, files);
    }

    @Override
    public void update(Long projectId, Long boardId, BoardUpdateRequest request) {
        Board board = getBoard(projectId, boardId);
        List<BoardFile> existingFiles = boardFileRepository
                .findByBoardIdAndDeletedYnOrderByCreatedAtAsc(boardId, "N");

        board.update(
                request.title(),
                request.content(),
                request.notice(),
                request.category(),
                request.tag()
        );

        if (request.deleteFileIds() != null && !request.deleteFileIds().isEmpty()) {
            existingFiles.stream()
                    .filter(file -> request.deleteFileIds().contains(file.getId()))
                    .forEach(BoardFile::delete);
        }

        saveAttachments(boardId, request.files());
    }

    @Override
    public void delete(Long projectId, Long boardId) {
        Board board = getBoard(projectId, boardId);
        board.delete();

        List<BoardFile> files = boardFileRepository
                .findByBoardIdAndDeletedYnOrderByCreatedAtAsc(boardId, "N");

        files.forEach(BoardFile::delete);
    }

    private Board getBoard(Long projectId, Long boardId) {
        return boardRepository
                .findByIdAndProjectIdAndDeletedYn(boardId, projectId, "N")
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoardListResponse> searchBoards(
            Long projectId,
            String keyword,
            Pageable pageable
    ) {
        return boardRepository.searchBoards(projectId, keyword, pageable)
                .map(BoardListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDetailModalResponse getModal(Long projectId, Long boardId) {

        Board board = getBoard(projectId, boardId);
        List<FileResponse> files = boardFileRepository
                .findByBoardIdAndDeletedYnOrderByCreatedAtAsc(boardId, "N")
                .stream()
                .map(file -> FileResponse.builder()
                        .id(file.getId())
                        .originalName(file.getOriginalName())
                        .storedName(file.getStoredName())
                        .fileUrl(file.getFileUrl())
                        .build())
                .toList();

        return BoardDetailModalResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .writerName(board.getWriterName())
                .viewCount(board.getViewCount())
                .notice("Y".equals(board.getNoticeYn())) // 또는 board.isNotice()
                .createdAt(
                        board.getCreatedAt() != null
                                ? board.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                                : "-"
                )
                .commentCount(0L)
                .files(files)
                .comments(List.of())
                .build();
    }
}
