package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import com.example.WorkTopus.projects.dto.response.NoticeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BoardService {

    Long create(Long projectId, BoardCreateRequest request, String loginEmail);

    Page<BoardListResponse> searchBoards(Long projectId, String keyword, String category, String sort, Pageable pageable);


    Page<BoardListResponse> findBoards(Long projectId, Pageable pageable);

    BoardDetailResponse findDetail(Long projectId, Long boardId);

    void update(Long projectId, Long boardId, BoardUpdateRequest request, String loginEmail);

    void delete(Long projectId, Long boardId, String loginEmail);


    BoardDetailResponse findEditableBoard(Long projectId, Long boardId, String loginUserId);

    Optional<NoticeResponse> getLatestNotice(Long projectId);

    boolean isWriter(
            Long projectId,
            Long boardId,
            String loginUserId
    );

    boolean canDelete(
            Long projectId,
            Long boardId,
            String loginUserId
    );



}
