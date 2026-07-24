package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.manage.repository.ManageMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 멤버 접근 권한을 검증하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectBoardAccessService {

    private final ManageMemberRepository manageMemberRepository;

    // 프로젝트 참여 여부 확인
    public void validateMember(Long workspaceId, String userId) {
        boolean member = manageMemberRepository
                .existsByWorkspaceIdAndUser_UserId(workspaceId, userId);

        if (!member) {
            throw new AccessDeniedException("해당 프로젝트에 접근할 권한이 없습니다.");
        }
    }
}
