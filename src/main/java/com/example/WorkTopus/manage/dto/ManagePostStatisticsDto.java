package com.example.WorkTopus.manage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ManagePostStatisticsDto {

    private long totalPosts;
    private long noticePosts;
    private long totalComments;
    private long totalFiles;
}
