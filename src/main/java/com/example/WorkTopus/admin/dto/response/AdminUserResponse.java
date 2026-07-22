package com.example.WorkTopus.admin.dto.response;

import com.example.WorkTopus.entity.Role;
import com.example.WorkTopus.entity.Users;

import java.time.LocalDateTime;

public record AdminUserResponse(

        Long userNum,
        String userId,
        String name,
        String email,
        Role role,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime deleteAt,

        String picture

) {

    public static AdminUserResponse from(Users user) {

        return new AdminUserResponse(
                user.getUserNum(),
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getDeleteAt(),
                user.getPicture()
        );
    }

}