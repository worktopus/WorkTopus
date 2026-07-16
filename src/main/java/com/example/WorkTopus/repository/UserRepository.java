package com.example.WorkTopus.repository;

import com.example.WorkTopus.entity.Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByUserId(String userId);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Users> findByUserId(String userId);

    Optional<Users> findByEmailIgnoreCase(String email);

}