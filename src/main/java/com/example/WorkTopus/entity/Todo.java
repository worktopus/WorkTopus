package com.example.WorkTopus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "todo")
@Getter
@Setter
@ToString(exclude = "user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {

    // 자동증가 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;

    // users 테이블
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_num", nullable = false)
    private Users user;

    // 내용
    @Column(nullable = false, length = 500)
    private String content;

    // 갱신을 위한 비즈니스 메서드 (Setter 대용)
    public void changeContent(String content) {
        this.content = content;
    }
}