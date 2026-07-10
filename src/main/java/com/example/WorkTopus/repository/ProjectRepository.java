package com.example.WorkTopus.repository;

import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Projects, Long> {

    Optional<Projects> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    List<Projects> findByOwnerOrderByCreatedAtDesc(Users owner);
}