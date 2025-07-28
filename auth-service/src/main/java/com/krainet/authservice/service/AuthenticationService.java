package com.krainet.authservice.service;

import com.krainet.authservice.dto.CreateUserRequest;
import com.krainet.authservice.dto.LoginRequest;
import com.krainet.authservice.dto.TokenResponse;
import com.krainet.authservice.dto.UserDto;

public interface AuthenticationService {
    TokenResponse authenticateUser(LoginRequest loginRequest);
    TokenResponse refreshToken(String refreshToken);
    void logout(String token);
    UserDto registerUser(CreateUserRequest createUserRequest) throws RuntimeException;
}
