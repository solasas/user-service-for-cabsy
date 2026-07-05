package com.cabsy.user.service;

import com.cabsy.user.dto.AuthResponse;
import com.cabsy.user.dto.LoginRequest;
import com.cabsy.user.dto.RegisterRequest;
import com.cabsy.user.dto.UserProfileResponse;
import com.cabsy.user.entity.User;
import com.cabsy.user.entity.UserRole;
import com.cabsy.user.exception.EmailAlreadyExistsException;
import com.cabsy.user.exception.InvalidCredentialsException;
import com.cabsy.user.exception.InvalidRequestException;
import com.cabsy.user.repository.UserRepository;
import com.cabsy.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        if (request.role() == UserRole.DRIVER) {
            boolean missingVehicleInfo = request.vehicleModel() == null || request.vehicleModel().isBlank()
                    || request.vehiclePlateNumber() == null || request.vehiclePlateNumber().isBlank();
            if (missingVehicleInfo) {
                throw new InvalidRequestException("vehicleModel and vehiclePlateNumber are required for drivers");
            }
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(request.role());
        if (request.role() == UserRole.DRIVER) {
            user.setVehicleModel(request.vehicleModel());
            user.setVehiclePlateNumber(request.vehiclePlateNumber());
        }

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());

        return new AuthResponse(token, saved.getId(), saved.getFullName(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getFullName(), user.getRole());
    }

    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getVehicleModel(),
                user.getVehiclePlateNumber(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}