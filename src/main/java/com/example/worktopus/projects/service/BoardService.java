package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.BoardDetailModalResponse;
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardService {

    Long create(Long projectId, BoardCreateRequest request);

    Page<BoardListResponse> findBoards(Long projectId, Pageable pageable);

    BoardDetailResponse findDetail(Long projectId, Long boardId);

    void update(Long projectId, Long boardId, BoardUpdateRequest request);

    void delete(Long projectId, Long boardId);

    Page<BoardListResponse> searchBoards(Long projectId, String keyword, Pageable pageable);

    BoardDetailModalResponse getModal(Long projectId, Long boardId);
}
