package com.krainet.authservice.service;

import com.krainet.authservice.dto.CreateUserRequest;
import com.krainet.authservice.dto.UpdateUserRequest;
import com.krainet.authservice.dto.UserDto;
import com.krainet.authservice.exception.NotFoundException;
import com.krainet.authservice.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    
    UserDto createUser(CreateUserRequest createUserRequest);
    
    UserDto getUserById(UUID id) throws NotFoundException;
    
    UserDto getCurrentUser();
    
    List<UserDto> getAllUsers();
    
    UserDto updateUser(UUID id, UpdateUserRequest updateUserRequest) throws NotFoundException;
    
    void deleteUser(UUID id) throws NotFoundException;
    
    User getCurrentUserEntity();
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
