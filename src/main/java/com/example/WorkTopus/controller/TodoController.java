package com.example.WorkTopus.controller;

import com.example.WorkTopus.entity.Todo;
import com.example.WorkTopus.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    // 유저 투두 조회
    @GetMapping
    public ResponseEntity<List<Todo>> getTodoList(Authentication authentication) {
        String userId = authentication.getName();
        List<Todo> list = todoService.getTodoList(userId);
        return ResponseEntity.ok(list);
    }

    // 투두 추가
    @PostMapping
    public ResponseEntity<Todo> addTodo(Authentication authentication, @RequestBody Map<String, String> request) {
        String userId = authentication.getName();
        String content = request.get("content"); // JSON에서 content 키값 추출
        Todo savedTodo = todoService.addTodo(userId, content);
        return ResponseEntity.ok(savedTodo);
    }

    // 투두 내용 수정 (PATCH)
    @PatchMapping("/{todoId}")
    public ResponseEntity<Void> updateTodo(
            @PathVariable Long todoId,
            @RequestBody Map<String, String> request) {

        String content = request.get("content"); // 프론트에서 보낸 수정한 텍스트 추출
        todoService.updateTodoContent(todoId, content);
        return ResponseEntity.ok().build();
    }

    // 투두 삭제
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long todoId) {
        todoService.deleteTodo(todoId);
        return ResponseEntity.ok().build();
    }
}