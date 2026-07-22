package com.example.WorkTopus.manage.repository;

import com.example.WorkTopus.manage.entity.ManageMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManageMemberRepository extends JpaRepository<ManageMember, Long> {

    // 특정 워크스페이스 ID에 소속된 팀원 목록을 전부 조회합니다.
    List<ManageMember> findByWorkspaceId(Long workspaceId);

    // 특정 워크스페이스의 로그인 사용자 조회
    Optional<ManageMember> findByWorkspaceIdAndUser_UserId(
            Long workspaceId,
            String userId
    );

    Optional<ManageMember> findByWorkspaceIdAndUser_Name(
            Long workspaceId,
            String name
    );


    boolean existsByWorkspaceIdAndUser_UserId(Long workspaceId, String userId);
}
