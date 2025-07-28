package com.krainet.authservice.controller;

import com.krainet.authservice.dto.CreateUserRequest;
import com.krainet.authservice.dto.LoginRequest;
import com.krainet.authservice.dto.TokenResponse;
import com.krainet.authservice.dto.UserDto;
import com.krainet.authservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        try {
            TokenResponse tokenResponse = authenticationService.authenticateUser(loginRequest);
            log.debug("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("Login failed for user: {} - {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        log.info("Registering new user with username: {}", createUserRequest.getUsername());
        try {
            UserDto createdUser = authenticationService.registerUser(createUserRequest);
            log.info("Successfully registered user with ID: {}", createdUser.getId());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("User registration failed for {} - {}", createUserRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        log.debug("Refreshing token");
        try {
            String token = refreshToken.substring(7); // Remove "Bearer " prefix
            TokenResponse tokenResponse = authenticationService.refreshToken(token);
            log.debug("Token refreshed successfully for user: {}", tokenResponse.getUser() != null ? tokenResponse.getUser().getUsername() : "unknown");
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout requested");
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            authenticationService.logout(jwt);
            log.debug("Logout successful");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw e;
        }
    }
}
