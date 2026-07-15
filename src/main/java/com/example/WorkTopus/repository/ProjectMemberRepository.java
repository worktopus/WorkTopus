package com.example.WorkTopus.repository;

import com.example.WorkTopus.entity.ProjectMember;
import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectAndUser(Projects project, Users user);

    List<ProjectMember> findByUserOrderByJoinedAtDesc(Users user);
}