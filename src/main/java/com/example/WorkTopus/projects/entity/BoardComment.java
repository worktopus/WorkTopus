package com.example.WorkTopus.projects.entity;

import com.example.WorkTopus.entity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECT_BOARD_COMMENT")
@Getter
@NoArgsConstructor
public class BoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOARD_ID", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WRITER_ID", nullable = false)
    private Users writer;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static BoardComment create(
            Board board,
            Users writer,
            String content
    ) {
        BoardComment comment = new BoardComment();
        comment.board = board;
        comment.writer = writer;
        comment.content = content;
        return comment;
    }

    public void update(String content) {
        this.content = content;
    }
}