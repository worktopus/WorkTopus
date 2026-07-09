package com.example.worktopus.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int currentPage,
        int startPage,
        int endPage,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last
) {

    private static final int PAGE_BLOCK_SIZE = 5;

    public static <T> PageResponse<T> from(Page<T> page) {
        int currentPage = page.getNumber() + 1;
        int totalPages = page.getTotalPages();

        int startPage = ((currentPage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, totalPages);

        return new PageResponse<>(
                page.getContent(),
                currentPage,
                startPage,
                endPage,
                totalPages,
                page.getTotalElements(),
                page.isFirst(),
                page.isLast()
        );
    }
}