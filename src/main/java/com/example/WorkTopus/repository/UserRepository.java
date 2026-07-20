package com.example.WorkTopus.repository;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.projects.entity.Board;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByUserId(String userId);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Users> findByUserId(String userId);

    Optional<Users> findByEmailIgnoreCase(String email);

    @Query("SELECT b FROM Board b WHERE b.writerName = :writerName AND b.deletedYn = 'N' ORDER BY b.id DESC")
    List<Board> findActiveBoardsByWriterName(@Param("writerName") String writerName);

}