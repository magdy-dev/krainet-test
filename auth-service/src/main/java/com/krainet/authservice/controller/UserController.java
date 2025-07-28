package com.krainet.authservice.controller;

import com.krainet.authservice.dto.CreateUserRequest;
import com.krainet.authservice.dto.UpdateUserRequest;
import com.krainet.authservice.dto.UserDto;
import com.krainet.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        log.info("Received request to create user with username: {}", createUserRequest.getUsername());
        try {
            UserDto createdUser = userService.createUser(createUserRequest);
            log.info("Successfully created user with ID: {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == principal.id)")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        log.debug("Fetching user with ID: {}", id);
        try {
            UserDto user = userService.getUserById(id);
            log.debug("Successfully retrieved user with ID: {}", id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to fetch user with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user details")
    public ResponseEntity<UserDto> getCurrentUser() {
        log.debug("Fetching current user details");
        try {
            log.debug("Successfully retrieved current user details");
            return ResponseEntity.ok(userService.getCurrentUser());
        } catch (Exception e) {
            log.error("Failed to fetch current user: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Fetching all users");
        try {
            List<UserDto> users = userService.getAllUsers();
            log.debug("Successfully retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Failed to fetch users: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == principal.id)")
    @Operation(summary = "Update user by ID")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        log.info("Received request to update user with ID: {}", id);
        try {
            UserDto updatedUser = userService.updateUser(id, updateUserRequest);
            log.info("Successfully updated user with ID: {}", id);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to update user with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == principal.id)")
    @Operation(summary = "Delete user by ID")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("Received request to delete user with ID: {}", id);
        try {
            userService.deleteUser(id);
            log.info("Successfully deleted user with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete user with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }
}
