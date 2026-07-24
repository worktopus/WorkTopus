package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.Notification.entity.NotificationType;
import com.example.WorkTopus.Notification.service.NotificationService;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.projects.dto.response.CommentResponse;
import com.example.WorkTopus.projects.entity.Board;
import com.example.WorkTopus.projects.entity.BoardComment;
import com.example.WorkTopus.projects.repository.BoardCommentRepository;
import com.example.WorkTopus.projects.repository.BoardRepository;
import com.example.WorkTopus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 게시글 댓글에 대한 비즈니스 로직을 처리하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final BoardRepository boardRepository;
    private final BoardCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public List<CommentResponse> findAll(Long boardId, String loginUserId) {
        // 댓글 목록을 조회하여 응답 객체로 변환
        return commentRepository
                .findByBoard_IdOrderByCreatedAtAsc(boardId)
                .stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .writerName(comment.getWriter().getName())
                        .content(comment.getContent())
                        .createdAt(
                                comment.getCreatedAt()
                                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                        )
                        .mine(
                                Objects.equals(
                                        comment.getWriter().getUserId(),
                                        loginUserId
                                )
                        )
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void create(Long projectId, Long boardId, String userId, String content) {
        // 게시글 및 작성자 조회
        Board board = boardRepository
                .findByIdAndProjectId(boardId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        Users writer = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        // 댓글 저장
        commentRepository.save(
                BoardComment.create(board, writer, content.trim())
        );

        // ================= [알림 생성 수정] =================
        // userId(이메일)로 먼저 찾아보고, 없으면 name(실명)으로 찾아옵니다.
        Users postWriter = userRepository.findByUserId(board.getWriterName())
                .orElseGet(() -> userRepository.findByName(board.getWriterName()).orElse(null));

        if (postWriter != null) {
            // 본인이 쓴 글에 본인이 댓글을 단 경우는 알림 제외
            if (!postWriter.getUserId().equals(writer.getUserId())) {
                String message = writer.getName() + "님이 회원님의 게시글에 댓글을 남겼습니다.";
                String url = "/projects/" + board.getProjectId() + "/boards/" + boardId;

                notificationService.createNotification(
                        postWriter,
                        message,
                        url,
                        NotificationType.COMMENT
                );
            }
        }
        // ===================================================

    }

    @Override
    @Transactional
    public void delete(Long projectId, Long boardId, Long commentId, String userId) {
        // 삭제 대상 댓글 조회
        BoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 없습니다."));

        // 프로젝트 및 게시글 권한 검증
        if (!comment.getBoard().getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("잘못된 프로젝트입니다.");
        }

        if (!comment.getBoard().getId().equals(boardId)) {
            throw new IllegalArgumentException("잘못된 게시글입니다.");
        }

        if (!comment.getWriter().getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }

    @Transactional
    public void update(Long projectId, Long boardId, Long commentId, String userId, String content
    ) {

        // 수정 대상 댓글 조회
        BoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("댓글이 없습니다.")
                );

        // 프로젝트 및 게시글 권한 검증
        if (!comment.getBoard().getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("잘못된 프로젝트입니다.");
        }

        if (!comment.getBoard().getId().equals(boardId)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        if (!comment.getWriter().getUserId().equals(userId)) {
            throw new IllegalStateException("본인 댓글만 수정 가능합니다.");
        }

        // 댓글 수정
        comment.update(content);
    }
}