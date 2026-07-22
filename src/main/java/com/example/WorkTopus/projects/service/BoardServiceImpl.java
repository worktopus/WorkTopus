package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.Notification.entity.NotificationType;
import com.example.WorkTopus.Notification.service.NotificationService;
import com.example.WorkTopus.entity.ProjectMember;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.projects.dto.request.BoardCreateRequest;
import com.example.WorkTopus.projects.dto.request.BoardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.*;
import com.example.WorkTopus.projects.entity.Board;
import com.example.WorkTopus.projects.entity.BoardFile;
import com.example.WorkTopus.projects.exception.BoardNotFoundException;
import com.example.WorkTopus.projects.repository.BoardCommentRepository;
import com.example.WorkTopus.projects.repository.BoardFileRepository;
import com.example.WorkTopus.projects.repository.BoardRepository;
import com.example.WorkTopus.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final FileStorageService fileStorageService;

    private final NotificationService notificationService;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public Long create(Long projectId, BoardCreateRequest request, Users loginUser) {

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

        // ================= [게시글 등록 알림 처리] =================
// 💡 SecurityContext에서 현재 로그인한 사용자 계정 ID 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = (authentication != null) ? authentication.getName() : null;

// 1. 해당 프로젝트의 팀원 목록 조회
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

// 2. 카테고리 한글 명칭 매핑 (선택 사항 - 알림 문구를 예쁘게 만들기 위함)
        String categoryName = switch (request.category()) {
            case "NOTICE" -> "공지사항";
            case "FREE" -> "자유게시판";
            case "IDEA" -> "아이디어";
            case "QUESTION" -> "질문게시판";
            default -> "게시판";
        };

// 3. 전체 팀원에게 알림 발송
        for (ProjectMember member : members) {
            Users targetUser = member.getUser();

            // 💡 [본인 제외 조건]
            boolean isSelfByEntity = (loginUser != null && targetUser.getUserNum().equals(loginUser.getUserNum()));
            boolean isSelfByUserId = (currentUserId != null && currentUserId.equals(targetUser.getUserId()));

            if (isSelfByEntity || isSelfByUserId) {
                continue; // 작성자 본인이면 제외
            }

            // 알림 메시지 구성 예시: [공지사항] 새로운 게시글이 등록되었습니다.
            String message = "[" + categoryName + "] " + savedBoard.getTitle() + " 글이 등록되었습니다.";
            String url = "/projects/" + projectId + "/boards/" + savedBoard.getId();

            // NotificationType은 기존 BOARD 또는 NOTICE/CATEGORY 타입을 활용
            notificationService.createNotification(
                    targetUser,
                    message,
                    url,
                    NotificationType.NOTICE // 또는 정의하신 BOARD / POST 타입
            );
        }
// =======================================================

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

        Page<Board> boards = boardRepository
                .findByProjectIdAndDeletedYnOrderByNoticeYnDescCreatedAtDesc(
                        projectId,
                        "N",
                        pageable
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

    @Override
    @Transactional(readOnly = true)
    public Page<BoardListResponse> searchBoards(
            Long projectId,
            String keyword,
            Pageable pageable
    ) {

        Page<Board> boards =
                boardRepository.searchBoards(projectId, keyword, pageable);

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
