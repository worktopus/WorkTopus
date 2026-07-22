package com.example.WorkTopus.projects.repository;

import com.example.WorkTopus.projects.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

<<<<<<< HEAD
=======
    // 목록 조회 + 페이징
>>>>>>> origin/feature/admin
    Page<Board> findByProjectIdAndDeletedYnOrderByNoticeYnDescCreatedAtDesc(
            Long projectId,
            String deletedYn,
            Pageable pageable
    );

<<<<<<< HEAD
    Page<Board> findByProjectIdAndDeletedYnOrderByCreatedAtDesc(
            Long projectId,
            String deletedYn,
            Pageable pageable
    );

    long countByProjectIdAndDeletedYn(
            Long projectId,
            String deletedYn
    );

=======
    // 상세/수정/삭제용 단건 조회
>>>>>>> origin/feature/admin
    Optional<Board> findByIdAndProjectIdAndDeletedYn(
            Long boardId,
            Long projectId,
            String deletedYn
    );

<<<<<<< HEAD
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
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(b.writerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(b.tag) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      AND (
            :category IS NULL
            OR b.category = :category
      )
=======
    // 검색
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
>>>>>>> origin/feature/admin
    """)
    Page<Board> searchBoards(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword,
<<<<<<< HEAD
            @Param("category") String category,
            Pageable pageable
    );

}
=======
            Pageable pageable
    );

    Optional<Board> findByIdAndProjectId(Long boardId, Long projectId);
}
>>>>>>> origin/feature/admin
