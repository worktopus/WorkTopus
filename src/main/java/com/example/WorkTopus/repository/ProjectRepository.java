package com.example.WorkTopus.repository;

import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Projects, Long> {

    Optional<Projects> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    List<Projects> findByOwnerOrderByCreatedAtDesc(Users owner);


    // 관리자 페이지 프로젝트 관리 검색 기능
    @Query("""
    SELECT p
    FROM Projects p
    WHERE
        (:keyword IS NULL OR :keyword = '')
        OR
        (
            :searchType = 'name'
            AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        OR
        (
            :searchType = 'leader'
            AND LOWER(p.owner.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    ORDER BY p.createdAt DESC
""")
    Page<Projects> searchProjects(
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}