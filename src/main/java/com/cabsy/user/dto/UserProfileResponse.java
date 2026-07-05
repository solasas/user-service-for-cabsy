package com.cabsy.user.dto;

import com.cabsy.user.entity.UserRole;

import java.time.Instant;

public record UserProfileResponse(
        String userId,
        String email,
        String fullName,
        String phoneNumber,
        UserRole role,
        String vehicleModel,
        String vehiclePlateNumber,
        boolean active,
        Instant createdAt
) {
}