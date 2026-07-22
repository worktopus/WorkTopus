package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
<<<<<<< HEAD
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import com.example.WorkTopus.projects.dto.response.NoticeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BoardService {

    Long create(Long projectId, BoardCreateRequest request, String loginEmail);

    Page<BoardListResponse> searchBoards(Long projectId, String keyword, String category, String sort, Pageable pageable);

=======
import com.example.WorkTopus.projects.dto.response.BoardDetailModalResponse;
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardService {

    Long create(BoardCreateRequest request);
>>>>>>> origin/feature/admin

    Page<BoardListResponse> findBoards(Long projectId, Pageable pageable);

    BoardDetailResponse findDetail(Long projectId, Long boardId);

<<<<<<< HEAD
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



=======
    void update(Long projectId, Long boardId, BoardUpdateRequest request);

    void delete(Long projectId, Long boardId);

    Page<BoardListResponse> searchBoards(Long projectId, String keyword, Pageable pageable);

    BoardDetailModalResponse getModal(Long projectId, Long boardId);
>>>>>>> origin/feature/admin
}
