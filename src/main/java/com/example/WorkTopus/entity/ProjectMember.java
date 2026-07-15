package com.example.WorkTopus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "PROJECT_MEMBER",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_PROJECT_MEMBER",
                        columnNames = {"PROJECT_ID", "USER_NUM"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Projects project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "USER_NUM",
            referencedColumnName = "USER_NUM",
            nullable = false
    )
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private ProjectRole role;

    @CreationTimestamp
    @Column(name = "JOINED_AT", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}