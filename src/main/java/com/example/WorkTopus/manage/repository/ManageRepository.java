package com.example.WorkTopus.manage.repository;

import com.example.WorkTopus.manage.entity.Manage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManageRepository extends JpaRepository<Manage, Long> {
    // Spring Data JPA에서 기본 CRUD 기능을 제공합니다.
}
