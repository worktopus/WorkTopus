package com.example.WorkTopus.projects.repository;

import com.example.WorkTopus.projects.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findByProjectIdAndDeletedYnOrderByNoticeYnDescCreatedAtDesc(
            Long projectId,
            String deletedYn,
            Pageable pageable
    );

    Page<Board> findByProjectIdAndDeletedYnOrderByCreatedAtDesc(
            Long projectId,
            String deletedYn,
            Pageable pageable
    );

    long countByProjectIdAndDeletedYn(
            Long projectId,
            String deletedYn
    );

    Optional<Board> findByIdAndProjectIdAndDeletedYn(
            Long boardId,
            Long projectId,
            String deletedYn
    );

    Optional<Board> findByIdAndProjectId(
            Long boardId,
            Long projectId
    );

    Optional<Board> findFirstByProjectIdAndNoticeYnAndDeletedYnOrderByCreatedAtDesc(
            Long projectId,
            String noticeYn,
            String deletedYn
    );

    @Query("""
        SELECT b
        FROM Board b
        WHERE b.projectId = :projectId
          AND b.deletedYn = 'N'
          AND (
              LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.writerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY b.noticeYn DESC, b.createdAt DESC
    """)
    Page<Board> searchBoards(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword,
            Pageable pageable
    );


}
