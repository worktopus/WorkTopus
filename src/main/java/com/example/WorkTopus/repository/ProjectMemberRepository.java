package com.example.WorkTopus.repository;

import com.example.WorkTopus.entity.ProjectMember;
import com.example.WorkTopus.entity.ProjectRole;
import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMember, Long> {

    /*
     * 프로젝트와 사용자 Entity 기준 참여 여부
     */
    boolean existsByProjectAndUser(
            Projects project,
            Users user
    );

    /*
     * 알림 서비스 등에서 사용하는 기본 프로젝트 팀원 조회
     */
    List<ProjectMember> findByProject_Id(Long projectId);

    /*
     * 로그인 사용자가 참여한 프로젝트 조회
     */
    @EntityGraph(attributePaths = {
            "project"
    })
    List<ProjectMember> findByUserOrderByJoinedAtDesc(
            Users user
    );

    /*
     * 특정 프로젝트의 전체 참여자 조회
     * user를 함께 조회해 DTO 변환 시 USERS 정보를 사용할 수 있게 합니다.
     */
    @EntityGraph(attributePaths = {
            "user"
    })
    List<ProjectMember> findByProject_IdOrderByJoinedAtAsc(
            Long projectId
    );

    /*
     * 프로젝트 번호와 사용자 번호로 참여자 한 명 조회
     */
    @EntityGraph(attributePaths = {
            "user"
    })
    Optional<ProjectMember> findByProject_IdAndUser_UserNum(
            Long projectId,
            Long userNum
    );

    /*
     * 프로젝트 참여 여부
     */
    boolean existsByProject_IdAndUser_UserNum(
            Long projectId,
            Long userNum
    );

    /*
     * 프로젝트 OWNER 여부
     */
    boolean existsByProject_IdAndUser_UserNumAndRole(
            Long projectId,
            Long userNum,
            ProjectRole role
    );

    /*
     * 프로젝트 참여자 수
     */
    long countByProject_Id(
            Long projectId
    );
}