package com.example.WorkTopus.service;

import com.example.WorkTopus.dto.ProjectCreateForm;
import com.example.WorkTopus.entity.ProjectMember;
import com.example.WorkTopus.entity.ProjectRole;
import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.ProjectMemberRepository;
import com.example.WorkTopus.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private static final String INVITE_CODE_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // 프로젝트 생성
    public Projects createProject(ProjectCreateForm form, Users loginUser) {

        Projects project = new Projects();
        project.setName(form.getName());
        project.setDescription(form.getDescription());
        project.setOwner(loginUser);
        project.setInviteCode(generateInviteCode());

        Projects savedProject = projectRepository.save(project);

        // 프로젝트 생성자를 프로젝트 멤버에도 OWNER로 등록
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUser(loginUser);
        ownerMember.setRole(ProjectRole.OWNER);

        projectMemberRepository.save(ownerMember);

        return savedProject;
    }

    // 초대 코드로 프로젝트 참여
    public void joinProject(String inviteCode, Users loginUser) {

        String normalizedCode = inviteCode.trim().toUpperCase();

        Projects project = projectRepository.findByInviteCode(normalizedCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 초대 코드입니다.")
                );

        if (projectMemberRepository.existsByProjectAndUser(project, loginUser)) {
            throw new IllegalArgumentException("이미 참여 중인 프로젝트입니다.");
        }

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(loginUser);
        projectMember.setRole(ProjectRole.MEMBER);

        projectMemberRepository.save(projectMember);
    }

    // 현재 사용자가 참여한 모든 프로젝트 조회
    @Transactional(readOnly = true)
    public List<Projects> findProjectsByUser(Users loginUser) {
        return projectMemberRepository
                .findByUserOrderByJoinedAtDesc(loginUser)
                .stream()
                .map(ProjectMember::getProject)
                .toList();
    }

    // 중복되지 않는 초대 코드 생성
    private String generateInviteCode() {
        SecureRandom random = new SecureRandom();
        String inviteCode;

        do {
            StringBuilder codeBuilder = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                int index = random.nextInt(INVITE_CODE_CHARS.length());
                codeBuilder.append(INVITE_CODE_CHARS.charAt(index));
            }

            inviteCode = codeBuilder.toString();

        } while (projectRepository.existsByInviteCode(inviteCode));

        return inviteCode;
    }
}