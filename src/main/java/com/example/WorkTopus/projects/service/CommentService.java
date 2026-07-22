package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

    List<CommentResponse> findAll(Long boardId, String loginUserid);

    void create(Long projectId, Long boardId, String userId, String content);

    void delete(Long projectId, Long boardId, Long commentId, String userId);

    void update(Long projectId, Long boardId, Long commentId, String userId, String content);
}