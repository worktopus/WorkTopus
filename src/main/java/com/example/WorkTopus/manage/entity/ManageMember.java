package com.example.WorkTopus.manage.entity;

// [교정] com.example.WorkTopus 아래 entity 폴더에 있는 Users를 가져옵니다.
import com.example.WorkTopus.entity.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECT_MEMBER")
@Getter
@Setter
@NoArgsConstructor
public class ManageMember {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manage_member_seq")
    @SequenceGenerator(name = "manage_member_seq", sequenceName = "PROJECT_MEMBER_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long workspaceId;

    @Column(name = "ROLE", nullable = false)
    private String projectRole;

    @Column(name = "JOINED_AT")
    private LocalDateTime joinedAt;

    @Column(name = "ASSIGNED_ROLE")
    private String assignedRole;

    // ==========================================
    // [핵심 추가] USERS 테이블과의 다대일(ManyToOne) 조인 연동
    // ==========================================
    @Column(name = "USER_NUM", nullable = false)
    private Long userNum;

    // 객체지향 조인 관계를 맺어 USERS 테이블의 데이터를 가져옵니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM", insertable = false, updatable = false)
    private Users user;


    // 기존 자바스크립트 변수명(member.userName) 호환을 위한 Getter 커스텀 설정
    public String getUserName() {
        return (this.user != null) ? this.user.getName() : "이름 없음";
    }

    // 기존 자바스크립트 변수명(member.userEmail) 호환을 위한 Getter 커스텀 설정
    public String getUserEmail() {
        return (this.user != null) ? this.user.getEmail() : "이메일 없음";
    }

    // 비즈니스 로직: 직급(역할) 변경
    public void updateProjectRole(String projectRole) {
        if (projectRole != null && !projectRole.trim().isEmpty()) {
            this.projectRole = projectRole.trim();
        }
    }
}
