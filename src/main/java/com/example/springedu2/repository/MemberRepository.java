package com.example.springedu2.repository;

import com.example.springedu2.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Member> findByUsername(String username);

    boolean existsByEmailAndIdNot(@NotBlank(message = "이메일은 필수입니다") @Email(message = "이메일 형식으로 입력하세요") @Size(max = 320, message = "이메일은 320자 이내로 입력하세요") String email, Long id);
}