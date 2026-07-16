package com.example.WorkTopus.repository;

import com.example.WorkTopus.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUser_UserIdOrderByTodoIdDesc(String userId);

}