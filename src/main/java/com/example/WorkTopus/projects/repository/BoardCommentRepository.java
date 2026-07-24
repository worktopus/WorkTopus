package com.example.WorkTopus.projects.repository;

import com.example.WorkTopus.projects.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardCommentRepository
        extends JpaRepository<BoardComment, Long> {

    @Query("""
        SELECT c.board.id, COUNT(c)
        FROM BoardComment c
        WHERE c.board.id IN :boardIds
        GROUP BY c.board.id
    """)
    List<Object[]> countCommentsByBoardIds(
            @Param("boardIds") List<Long> boardIds
    );

    List<BoardComment> findByBoard_IdOrderByCreatedAtAsc(Long boardId);

}