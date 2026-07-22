package com.example.WorkTopus.repository;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.projects.entity.Board;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByUserId(String userId);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Users> findByUserId(String userId);

    Optional<Users> findByEmailIgnoreCase(String email);

    @Query("SELECT b, p.name, b.projectId FROM Board b " +
            "JOIN Projects p ON b.projectId = p.id " +
            "WHERE b.writerName = :writerName AND b.deletedYn = 'N' " +
            "ORDER BY b.id DESC")
    List<Object[]> findActiveBoardsWithProjectNameByWriterName(@Param("writerName") String writerName);


    // 관리자 페이지
    long countByEnabledTrue();

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime start,
            LocalDateTime end
    );


    long countByEnabledFalse();

    long countByDeleteAtIsNotNull();

    Page<Users> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT u
            FROM Users u
            WHERE (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.userId) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (
                :status IS NULL
                OR :status = ''
                OR (:status = 'ACTIVE' AND u.enabled = true AND u.deleteAt IS NULL)
                OR (:status = 'INACTIVE' AND u.enabled = false AND u.deleteAt IS NULL)
                OR (:status = 'WITHDRAWN' AND u.deleteAt IS NOT NULL)
            )
            ORDER BY u.createdAt DESC
            """)
    Page<Users> searchAdminUsers(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );

}