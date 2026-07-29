package com.example.WorkTopus.manage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ManagePostPageDto {

    private List<ManagePostListItemDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean hasNext() {
        return page + 1 < totalPages;
    }
}
