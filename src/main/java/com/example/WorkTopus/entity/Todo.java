package com.example.WorkTopus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    // 완료 체크
    @Column(name = "is_completed", nullable = false)
    @JsonProperty("isCompleted")
    private boolean isCompleted = false;

    public void changeUpdate(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

}