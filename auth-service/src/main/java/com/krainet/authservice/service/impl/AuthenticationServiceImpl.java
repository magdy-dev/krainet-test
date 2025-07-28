package com.krainet.authservice.service.impl;

import com.krainet.authservice.dto.LoginRequest;
import com.krainet.authservice.dto.TokenResponse;
import com.krainet.authservice.dto.UserDto;
import com.krainet.authservice.dto.CreateUserRequest;

import com.krainet.authservice.security.JwtTokenProvider;
import com.krainet.authservice.service.AuthenticationService;
import com.krainet.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public UserDto registerUser(CreateUserRequest createUserRequest) throws RuntimeException {
        log.debug("Registering new user with username: {}", createUserRequest.getUsername());
        
        // Check if username already exists
        if (userService.existsByUsername(createUserRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        // Check if email already exists
        if (userService.existsByEmail(createUserRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Encode the password before passing to userService
        createUserRequest.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        
        // Save the user using the service
        return userService.createUser(createUserRequest);
    }
    
    @Override
    public TokenResponse authenticateUser(LoginRequest loginRequest) {
        log.debug("Authenticating user: {}", loginRequest.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = tokenProvider.generateToken(authentication);
        
        TokenResponse response = new TokenResponse();
        response.setAccessToken(jwt);
        response.setTokenType("Bearer");
        
        log.info("User {} authenticated successfully", loginRequest.getUsername());
        return response;
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {

        throw new UnsupportedOperationException("Token refresh not implemented yet");
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user");
    }


}
