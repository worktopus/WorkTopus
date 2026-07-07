package com.example.springedu2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity                // db table
@Table(name="members") // table 이름 변경
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Member { // 회원

    @Id                                                  // primarykey 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 번호자동증가
    private Long id;

    @Column(nullable = false, updatable = true, length = 30)  // notnull, unique, varchar(30)
    private String username;

    @Column(nullable = false) // 암호화된 비번은 길어짐 length 지정안함
    private String password;  // 로그인 비번

    @Column(nullable = false, length = 50)
    private String name;             // 사용자 이름

    @Column(nullable = false)
    private String email;            // 이메일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role = Role.USER;   // 권한

    @Column(nullable = false)
    private boolean enabled = true;  // 계정 사용 가능

    @CreationTimestamp                // 자동
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 계정 생성일, 가입일

    @CreationTimestamp
    @Column(nullable = false)         // 자동
    private LocalDateTime updatedAt;  // 계정 수정일
}
