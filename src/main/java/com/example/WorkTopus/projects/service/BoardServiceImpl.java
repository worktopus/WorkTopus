package com.example.WorkTopus.projects.service;

<<<<<<< HEAD
import com.example.WorkTopus.manage.entity.ManageMember;
import com.example.WorkTopus.manage.repository.ManageMemberRepository;
import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.*;
import com.example.WorkTopus.projects.entity.Board;
import com.example.WorkTopus.projects.entity.BoardFile;
import com.example.WorkTopus.projects.exception.BoardNotFoundException;
import com.example.WorkTopus.projects.repository.BoardCommentRepository;
import com.example.WorkTopus.projects.repository.BoardFileRepository;
import com.example.WorkTopus.projects.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
=======
import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.BoardDetailModalResponse;
import com.example.WorkTopus.projects.dto.response.BoardDetailResponse;
import com.example.WorkTopus.projects.dto.response.BoardListResponse;
import com.example.WorkTopus.projects.entity.Board;
import com.example.WorkTopus.projects.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
>>>>>>> origin/feature/admin

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
<<<<<<< HEAD
    private final BoardFileRepository boardFileRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final FileStorageService fileStorageService;
    private final ManageMemberRepository manageMemberRepository;

    @Override
    public Long create(
            Long projectId,
            BoardCreateRequest request,
            String loginUserId
    ) {
        ManageMember member = manageMemberRepository
                .findByWorkspaceIdAndUser_UserId(
                        projectId,
                        loginUserId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 프로젝트의 멤버가 아닙니다."
                        )
                );

        boolean notice = "NOTICE".equals(request.category());

        Board board = new Board(
                projectId,
                request.title(),
                request.content(),
                member.getUserName(),
                notice,
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
=======

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
>>>>>>> origin/feature/admin
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoardListResponse> findBoards(Long projectId, Pageable pageable) {
<<<<<<< HEAD

        Page<Board> boards = boardRepository
=======
        return boardRepository
>>>>>>> origin/feature/admin
                .findByProjectIdAndDeletedYnOrderByNoticeYnDescCreatedAtDesc(
                        projectId,
                        "N",
                        pageable
<<<<<<< HEAD
                );

        Map<Long, Long> commentCountMap =
                getCommentCountMap(boards.getContent());

        return boards.map(board ->
                BoardListResponse.from(
                        board,
                        commentCountMap.getOrDefault(board.getId(), 0L)
                )
        );
    }
    @Override
    @Transactional(readOnly = true)
    public Page<BoardListResponse> searchBoards(
            Long projectId,
            String keyword,
            String category,
            String sort,
            Pageable pageable
    ) {
        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        String normalizedCategory =
                category == null || category.isBlank()
                        ? null
                        : category.trim();

        Pageable searchPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                createSearchSort(sort)
        );


        Page<Board> boards = boardRepository.searchBoards(
                projectId,
                normalizedKeyword,
                normalizedCategory,
                searchPageable
        );

        Map<Long, Long> commentCountMap =
                getCommentCountMap(boards.getContent());

        return boards.map(board ->
                BoardListResponse.from(
                        board,
                        commentCountMap.getOrDefault(board.getId(), 0L)
                )
        );
    }
    private Sort createSearchSort(String sort) {
        if ("views".equals(sort)) {
            return Sort.by(
                    Sort.Order.desc("noticeYn"),
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createdAt")
            );
        }

        return Sort.by(
                Sort.Order.desc("noticeYn"),
                Sort.Order.desc("createdAt")
        );
=======
                )
                .map(BoardListResponse::from);
>>>>>>> origin/feature/admin
    }

    @Override
    public BoardDetailResponse findDetail(Long projectId, Long boardId) {
        Board board = getBoard(projectId, boardId);
        board.increaseViewCount();

<<<<<<< HEAD
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


        String writerAssignedRole = manageMemberRepository
                .findByWorkspaceIdAndUser_Name(
                        projectId,
                        board.getWriterName()
                )
                .map(ManageMember::getAssignedRole)
                .orElse("담당 미지정");

        return BoardDetailResponse.from(
                board,
                files,
                writerAssignedRole
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDetailResponse findEditableBoard(
            Long projectId,
            Long boardId,
            String loginUserId
    ) {
        Board board = getBoard(projectId, boardId);

        ManageMember member = manageMemberRepository
                .findByWorkspaceIdAndUser_UserId(
                        projectId,
                        loginUserId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "프로젝트 멤버가 아닙니다."
                        )
                );

        if (!board.getWriterName().equals(member.getUserName())) {
            throw new IllegalArgumentException(
                    "작성자 본인만 수정할 수 있습니다."
            );
        }

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

        String writerAssignedRole = member.getAssignedRole();

        return BoardDetailResponse.from(board, files, writerAssignedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWriter(
            Long projectId,
            Long boardId,
            String loginUserId
    ) {
        Board board = getBoard(projectId, boardId);

        ManageMember member = manageMemberRepository
                .findByWorkspaceIdAndUser_UserId(
                        projectId,
                        loginUserId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "프로젝트 멤버가 아닙니다."
                        )
                );

        return board.getWriterName()
                .equals(member.getUserName());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDelete(
            Long projectId,
            Long boardId,
            String loginUserId
    ) {
        Board board = getBoard(projectId, boardId);

        ManageMember member = manageMemberRepository
                .findByWorkspaceIdAndUser_UserId(
                        projectId,
                        loginUserId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "프로젝트 멤버가 아닙니다."
                        )
                );

        boolean isWriter =
                board.getWriterName()
                        .equals(member.getUserName());

        boolean isProjectOwner =
                "OWNER".equals(member.getProjectRole());

        return isWriter || isProjectOwner;
    }

    @Override
    public void update(
            Long projectId,
            Long boardId,
            BoardUpdateRequest request,
            String loginUserId
    ) {
        Board board = getBoard(projectId, boardId);

        ManageMember member = manageMemberRepository
                .findByWorkspaceIdAndUser_UserId(
                        projectId,
                        loginUserId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "프로젝트 멤버가 아닙니다."
                        )
                );

        boolean isWriter =
                board.getWriterName().equals(member.getUserName());

        if (!isWriter) {
            throw new IllegalArgumentException(
                    "작성자 본인만 수정할 수 있습니다."
            );
        }

        List<BoardFile> existingFiles = boardFileRepository
                .findByBoardIdAndDeletedYnOrderByCreatedAtAsc(
                        boardId,
                        "N"
                );

        boolean notice = "NOTICE".equals(request.category());

        board.update(
                request.title(),
                request.content(),
                notice,
                request.category(),
                request.tag()
        );

        if (request.deleteFileIds() != null
                && !request.deleteFileIds().isEmpty()) {

            existingFiles.stream()
                    .filter(file ->
                            request.deleteFileIds().contains(file.getId())
                    )
                    .forEach(BoardFile::delete);
        }

        saveAttachments(boardId, request.files());
    }

    @Override
    public void delete(
            Long projectId,
            Long boardId,
            String loginUserId
    ) {
        Board board = getBoard(projectId, boardId);

        ManageMember member = manageMemberRepository
                .findByWorkspaceIdAndUser_UserId(
                        projectId,
                        loginUserId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 프로젝트의 멤버가 아닙니다."
                        )
                );

        boolean isWriter =
                board.getWriterName().equals(member.getUserName());

        boolean isProjectOwner =
                "OWNER".equals(member.getProjectRole());

        if (!isWriter && !isProjectOwner) {
            throw new IllegalArgumentException(
                    "작성자 또는 프로젝트 팀장만 삭제할 수 있습니다."
            );
        }

        board.delete();

        List<BoardFile> files = boardFileRepository
                .findByBoardIdAndDeletedYnOrderByCreatedAtAsc(
                        boardId,
                        "N"
                );

        files.forEach(BoardFile::delete);
=======
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
>>>>>>> origin/feature/admin
    }

    private Board getBoard(Long projectId, Long boardId) {
        return boardRepository
                .findByIdAndProjectIdAndDeletedYn(boardId, projectId, "N")
<<<<<<< HEAD
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다."));
    }

    private Map<Long, Long> getCommentCountMap(List<Board> boards) {

        if (boards == null || boards.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> boardIds = boards.stream()
                .map(Board::getId)
                .toList();

        return boardCommentRepository
                .countCommentsByBoardIds(boardIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }


    public Optional<NoticeResponse> getLatestNotice(Long projectId) {
        return boardRepository
                .findFirstByProjectIdAndNoticeYnAndDeletedYnOrderByCreatedAtDesc(
                        projectId,
                        "Y",
                        "N"
                )
                .map(NoticeResponse::from);
    }
}
=======
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
>>>>>>> origin/feature/admin
