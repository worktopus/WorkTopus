package com.example.WorkTopus.manage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "MANAGE_MEMBERS")
@Getter
@Setter
@NoArgsConstructor
public class ManageMember {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manage_member_seq")
    @SequenceGenerator(name = "manage_member_seq", sequenceName = "MANAGE_MEMBERS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "WORKSPACE_ID", nullable = false)
    private Long workspaceId;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @Column(name = "USER_EMAIL", nullable = false)
    private String userEmail;

    @Column(name = "PROJECT_ROLE", nullable = false)
    private String projectRole; // LEADER, SUB_LEADER, MEMBER 등

    @Column(name = "ASSIGNED_ROLE")
    private String assignedRole; // Backend Developer, UI Designer 등

    @Column(name = "JOINED_AT")
    private LocalDateTime joinedAt;

    // 비즈니스 로직: 직급(역할) 변경
    public void updateProjectRole(String projectRole) {
        if (projectRole != null && !projectRole.trim().isEmpty()) {
            this.projectRole = projectRole.trim();
        }
    }
}
