package com.example.WorkTopus.admin.dto.response;

import java.time.LocalDateTime;

public record AdminProjectResponse(

        Long id,
        String name,
        String description,
        String ownerName,
        long memberCount,
        LocalDateTime createdAt

) {}