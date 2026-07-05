package com.cabsy.user.dto;

import com.cabsy.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8)
        String password,

        @NotBlank
        String fullName,

        @NotBlank
        String phoneNumber,

        @NotNull
        UserRole role,

        String vehicleModel,

        String vehiclePlateNumber
) {
}