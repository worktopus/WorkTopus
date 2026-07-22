package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import com.example.WorkTopus.projects.dto.response.NoticeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BoardService {

    // 게시글 작성
    Long create(Long projectId, BoardCreateRequest request, String loginEmail);

    // 게시글 검색 (키워드, 카테고리, 정렬)
    Page<BoardListResponse> searchBoards(Long projectId, String keyword, String category, String sort, Pageable pageable);

    // 게시글 목록 조회
    Page<BoardListResponse> findBoards(Long projectId, Pageable pageable);

    // 게시글 상세 조회
    BoardDetailResponse findDetail(Long projectId, Long boardId);

    // 게시글 수정
    void update(Long projectId, Long boardId, BoardUpdateRequest request, String loginEmail);

    // 게시글 삭제
    void delete(Long projectId, Long boardId, String loginEmail);

    // 수정용 게시글 조회 (권한 검증 포함)
    BoardDetailResponse findEditableBoard(Long projectId, Long boardId, String loginUserId);

    // 최신 공지사항 조회
    Optional<NoticeResponse> getLatestNotice(Long projectId);

    // 작성자 여부 확인
    boolean isWriter(
            Long projectId,
            Long boardId,
            String loginUserId
    );

    // 삭제 권한 여부 확인
    boolean canDelete(
            Long projectId,
            Long boardId,
            String loginUserId
    );
}