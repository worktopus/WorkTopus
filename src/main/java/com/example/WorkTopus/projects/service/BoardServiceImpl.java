package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.Notification.entity.NotificationType;
import com.example.WorkTopus.Notification.service.NotificationService;
import com.example.WorkTopus.entity.ProjectMember;
import com.example.WorkTopus.entity.Users;
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
import com.example.WorkTopus.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final ManageMemberRepository manageMemberRepository;

    private final NotificationService notificationService;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public Long create(
            Long projectId,
            BoardCreateRequest request,
            String loginUserId
    ) {
        // 프로젝트 멤버 여부 확인
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

        // 게시글 생성 및 저장
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

        // 첨부파일 저장
        saveAttachments(savedBoard.getId(), request.files());

        // ================= [게시글 등록 알림 처리] =================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = (authentication != null) ? authentication.getName() : loginUserId;

        // 1) 해당 프로젝트의 팀원 목록 조회
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

        // 작성자 본인을 제외한 실제 수신 대상자만 필터링
        List<ProjectMember> targetMembers = members.stream()
                .filter(m -> currentUserId != null && !currentUserId.equals(m.getUser().getUserId()))
                .toList();

        // 수신할 팀원이 아무도 없다면 (혼자 프로젝트를 진행 중이라면) 알림 생성을 안 하고 건너뜁니다.
        if (targetMembers.isEmpty()) {
            return savedBoard.getId();
        }

        // 2) 카테고리 한글 명칭 매핑 (공지사항 -> 공지로 변경)
        String categoryName = switch (request.category()) {
            case "NOTICE" -> "공지";
            case "MEETING" -> "회의";
            case "WORK" -> "업무";
            case "RESOURCE" -> "자료";
            case "IDEA" -> "아이디어";
            case "ETC" -> "기타";
            default -> "게시판";
        };

        // 알림 메시지 포맷 (공통 변수 1회만 정의)
        String message = "[" + categoryName + "] " + savedBoard.getTitle() + " 글이 등록되었습니다.";
        String url = "/projects/" + projectId + "/boards/" + savedBoard.getId();

        // 3) 실제 수신 대상 팀원들에게만 알림 발송
        for (ProjectMember projectMember : targetMembers) {
            Users targetUser = projectMember.getUser();

            notificationService.createNotification(
                    targetUser,
                    message,
                    url,
                    NotificationType.NOTICE
            );
        }
        // =======================================================

        return savedBoard.getId();
    }

    private void saveAttachments(Long boardId, List<MultipartFile> files) {
        // 첨부파일이 없으면 저장 로직을 수행하지 않음
        if (files == null || files.isEmpty()) {
            return;
        }

        for (MultipartFile file : files) {
            // 비어 있는 파일은 저장 대상에서 제외
            if (file == null || file.isEmpty()) {
                continue;
            }

            // 실제 파일 저장 후 반환된 정보를 게시글 첨부파일 엔티티로 변환
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
        // 공지사항을 우선 배치하고 최신순으로 게시글 조회
        Page<Board> boards = boardRepository
                .findByProjectIdAndDeletedYnOrderByNoticeYnDescCreatedAtDesc(
                        projectId,
                        "N",
                        pageable
                );

        // 게시글별 댓글 수를 한 번에 조회해 응답 DTO에 포함
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
        // 빈 검색 조건은 null로 정규화하여 선택 조건으로 처리
        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        String normalizedCategory =
                category == null || category.isBlank()
                        ? null
                        : category.trim();

        // 요청된 정렬 기준을 적용한 검색용 Pageable 생성
        Pageable searchPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                createSearchSort(sort)
        );

        // 정규화된 검색 조건으로 게시글 조회
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
        // 공지사항을 우선 배치한 뒤 조회수 또는 최신순으로 정렬
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
    }

    @Override
    public BoardDetailResponse findDetail(Long projectId, Long boardId) {
        Board board = getBoard(projectId, boardId);
        // 상세 조회 시 게시글 조회수 증가
        board.increaseViewCount();

        // 삭제되지 않은 첨부파일만 조회
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

        // 게시글 작성자의 프로젝트 담당 역할 조회
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

        // 작성자 본인만 수정 화면에 접근 가능
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

        // 게시글 작성자 또는 프로젝트 팀장에게 삭제 권한 부여
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

        // 게시글 작성자만 수정 가능
        if (!isWriter) {
            throw new IllegalArgumentException(
                    "작성자 본인만 수정할 수 있습니다."
            );
        }

        // 삭제 요청된 첨부파일을 확인하기 위해 기존 파일 조회
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

        // 사용자가 삭제 대상으로 선택한 첨부파일을 논리 삭제
        if (request.deleteFileIds() != null
                && !request.deleteFileIds().isEmpty()) {

            existingFiles.stream()
                    .filter(file ->
                            request.deleteFileIds().contains(file.getId())
                    )
                    .forEach(BoardFile::delete);
        }

        // 새로 추가된 첨부파일 저장
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

        // 작성자 또는 프로젝트 팀장만 게시글 삭제 가능
        if (!isWriter && !isProjectOwner) {
            throw new IllegalArgumentException(
                    "작성자 또는 프로젝트 팀장만 삭제할 수 있습니다."
            );
        }

        // 게시글 논리 삭제
        board.delete();

        // 게시글에 연결된 첨부파일도 함께 논리 삭제
        List<BoardFile> files = boardFileRepository
                .findByBoardIdAndDeletedYnOrderByCreatedAtAsc(
                        boardId,
                        "N"
                );

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

        // 댓글 수를 일괄 조회하기 위해 게시글 ID 목록 추출
        List<Long> boardIds = boards.stream()
                .map(Board::getId)
                .toList();

        // 조회 결과를 게시글 ID와 댓글 수 형태의 Map으로 변환
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