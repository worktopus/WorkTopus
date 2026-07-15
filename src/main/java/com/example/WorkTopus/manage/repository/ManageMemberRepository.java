package com.example.WorkTopus.manage.repository;

import com.example.WorkTopus.manage.entity.ManageMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ManageMemberRepository extends JpaRepository<ManageMember, Long> {

    // 특정 워크스페이스 ID에 소속된 팀원 목록을 전부 조회합니다.
    List<ManageMember> findByWorkspaceId(Long workspaceId);
}
