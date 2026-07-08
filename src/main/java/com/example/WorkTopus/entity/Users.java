package com.example.WorkTopus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Users {

    @Id                                                  // primarykey
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 시퀀스
    private Long user_num;

    @Column(nullable = false, updatable = true, length = 30)  // notnull, unique, varchar(30)
    private String user_id;

    @Column(nullable = false)        // 암호화된 비번은 길어서 length 지정 X
    private String password;

    @Column(nullable = false, length = 50)
    private String name;              // 사용자 이름

    @Column(nullable = false)
    private String email;             // 이메일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role = Role.USER;    // 권한

    @Column(nullable = false)
    private boolean enabled = true;   // 계정 사용 가능

    @CreationTimestamp                // 자동
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 계정 생성일

    @CreationTimestamp
    @Column(nullable = false)         // 자동
    private LocalDateTime updatedAt;  // 계정 수정일

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime deleteAt;   // 계정 삭제일

}
