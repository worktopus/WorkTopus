package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.request.BoardCreateRequest;
import com.example.worktopus.projects.dto.request.BoardUpdateRequest;
import com.example.worktopus.projects.dto.response.BoardDetailModalResponse;
import com.example.worktopus.projects.dto.response.BoardDetailResponse;
import com.example.worktopus.projects.dto.response.BoardListResponse;
import com.example.worktopus.projects.entity.Board;
import com.example.worktopus.projects.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    @Override
    public Long create(BoardCreateRequest request) {
        Board board = new Board(
                request.projectId(),
                request.title(),
                request.content(),
                "관리자",
                request.notice(),
                request.category()
        );

        return boardRepository.save(board).getId();
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

        return BoardDetailResponse.from(board);
    }

    @Override
    public void update(Long projectId, Long boardId, BoardUpdateRequest request) {
        Board board = getBoard(projectId, boardId);

        board.update(
                request.title(),
                request.content(),
                request.notice(),
                request.category()
        );
    }

    @Override
    public void delete(Long projectId, Long boardId) {
        Board board = getBoard(projectId, boardId);
        board.delete();
    }

    private Board getBoard(Long projectId, Long boardId) {
        return boardRepository
                .findByIdAndProjectIdAndDeletedYn(boardId, projectId, "N")
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
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

        Board board = boardRepository.findByIdAndProjectId(boardId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

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
                .files(List.of())
                .comments(List.of())
                .build();
    }
}