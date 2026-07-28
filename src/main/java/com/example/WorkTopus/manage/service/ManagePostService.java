package com.example.WorkTopus.manage.service;

import com.example.WorkTopus.manage.dto.ManagePostPageDto;
import com.example.WorkTopus.manage.dto.ManagePostStatisticsDto;
import com.example.WorkTopus.manage.repository.ManagePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagePostService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final ManagePostRepository managePostRepository;

    public ManagePostStatisticsDto getStatistics(Long projectId) {
        return managePostRepository.findStatistics(projectId);
    }

    public ManagePostPageDto getPosts(
            Long projectId,
            String category,
            String keyword,
            Integer page,
            Integer size
    ) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null ? DEFAULT_PAGE_SIZE : Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        return managePostRepository.findPosts(
                projectId,
                normalizeCategory(category),
                normalizeKeyword(keyword),
                safePage,
                safeSize
        );
    }

    private String normalizeCategory(String category) {
        return category == null || category.isBlank() ? "ALL" : category.trim().toUpperCase();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }
}
