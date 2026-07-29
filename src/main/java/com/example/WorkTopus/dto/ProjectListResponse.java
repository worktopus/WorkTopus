package com.example.WorkTopus.dto;

public record ProjectListResponse(
        Long id,
        String name,
        String description,
        String inviteCode,
        int completionRate
) {
    public boolean completed() {
        return completionRate >= 100;
    }
}