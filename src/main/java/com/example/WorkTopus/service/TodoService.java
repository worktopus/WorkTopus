package com.example.WorkTopus.service;

import com.example.WorkTopus.entity.Todo;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.TodoRepository;
import com.example.WorkTopus.repository.UserRepository; // 유저 리포지토리 가정
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    // 유저 투두 조회
    public List<Todo> getTodoList(String userId) {
        return todoRepository.findByUser_UserIdOrderByTodoIdDesc(userId);
    }

    // 투두 추가
    @Transactional
    public Todo addTodo(String userId, String content) {
        // 유저 조회
        Users user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Todo todo = Todo.builder()
                .content(content)
                .user(user)
                .isCompleted(false)
                .build();

        return todoRepository.save(todo);
    }

    // 투두 수정
    @Transactional
    public void toggleTodo(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투두입니다."));

        todo.changeUpdate(!todo.isCompleted());
    }

    // 4. 투두 삭제
    @Transactional
    public void deleteTodo(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투두입니다."));

        todoRepository.delete(todo);
    }
}