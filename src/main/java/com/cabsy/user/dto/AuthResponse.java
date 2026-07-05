package com.cabsy.user.dto;

import com.cabsy.user.entity.UserRole;

public record AuthResponse(
        String token,
        String userId,
        String fullName,
        UserRole role
) {
}