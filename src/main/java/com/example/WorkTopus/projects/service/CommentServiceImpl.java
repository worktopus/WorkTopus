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
    public void create(Long boardId, String userId, String content) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        Users writer = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        commentRepository.save(
                BoardComment.create(board, writer, content.trim())
        );

        // ================= [🔍 디버깅용 로그 추가] =================
        System.out.println("========== [알림 디버깅 시작] ==========");
        System.out.println("1. 현재 댓글 작성자 ID: " + userId);
        System.out.println("2. 게시글에 저장된 writerName 값: " + board.getWriterName());

        userRepository.findByUserId(board.getWriterName()).ifPresentOrElse(
                postWriter -> {
                    System.out.println("3. 게시글 작성자 엔티티 찾음! ID: " + postWriter.getUserId());

                    if (!postWriter.getUserId().equals(writer.getUserId())) {
                        System.out.println("4. 작성자와 댓글자가 다르므로 알림 생성 실행!");

                        String message = writer.getName() + "님이 회원님의 게시글에 댓글을 남겼습니다.";
                        String url = "/projects/" + board.getProjectId() + "/boards/" + boardId;

                        notificationService.createNotification(
                                postWriter,
                                message,
                                url,
                                NotificationType.COMMENT
                        );
                        System.out.println("5. 알림 생성 완료!");
                    } else {
                        System.out.println("4-FAIL. 본인이 쓴 글에 본인이 댓글을 달았기 때문에 알림 스킵됨!");
                    }
                },
                () -> {
                    System.out.println("3-FAIL. board.getWriterName()으로 유저를 찾지 못했습니다! (값이 잘못되었거나 매칭 안됨)");
                }
        );
        System.out.println("========== [알림 디버깅 끝] ==========");
        // ========================================================

    }

    @Override
    @Transactional
    public void delete(Long boardId, Long commentId, String userId) {
        BoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 없습니다."));

        if (!comment.getBoard().getId().equals(boardId)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        if (!comment.getWriter().getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void update(Long boardId, Long commentId, String userId, String content
    ) {

        BoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("댓글이 없습니다.")
                );

        if (!comment.getBoard().getId().equals(boardId)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        if (!comment.getWriter().getUserId().equals(userId)) {
            throw new IllegalStateException("본인 댓글만 수정 가능합니다.");
        }

        comment.update(content);
    }
}