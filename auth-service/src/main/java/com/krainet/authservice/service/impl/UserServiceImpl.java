package com.krainet.authservice.service.impl;

import com.krainet.authservice.dto.CreateUserRequest;
import com.krainet.authservice.dto.UpdateUserRequest;
import com.krainet.authservice.dto.UserDto;
import com.krainet.authservice.exception.AlreadyExistsException;
import com.krainet.authservice.exception.NotFoundException;
import com.krainet.authservice.mapper.UserMapper;
import com.krainet.authservice.model.User;
import com.krainet.authservice.repository.UserRepository;
import com.krainet.authservice.service.UserService;
import com.krainet.authservice.service.event.UserEventProducer;
import com.krainet.common.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found with id: ";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserEventProducer userEventProducer;
    private final UserService userService;
    
    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest createUserRequest) {
        log.info("Starting to create new user with username: {}", createUserRequest.getUsername());
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            log.warn("Attempt to create user with existing username: {}", createUserRequest.getUsername());
            throw new AlreadyExistsException("Username already exists");
        }
        
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            log.warn("Attempt to create user with existing email: {}", createUserRequest.getEmail());
            throw new AlreadyExistsException("Email already in use");
        }
        
        log.debug("Mapping CreateUserRequest to User entity");
        User user = userMapper.toEntity(createUserRequest);
        
        log.debug("Encoding password for user: {}", createUserRequest.getUsername());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        
        log.debug("Saving new user to database");
        User savedUser = userRepository.save(user);
        
        log.info("Successfully created new user with ID: {}, username: {}, role: {}", 
                savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
        
        // Publish user created event
        try {
            userEventProducer.publishUserEvent(EventType.USER_CREATED, savedUser);
        } catch (Exception e) {
            log.error("Failed to publish USER_CREATED event for user {}", savedUser.getUsername(), e);
            // Don't fail the operation if event publishing fails
        }
        
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) throws NotFoundException {
        log.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new NotFoundException(USER_NOT_FOUND_MESSAGE + id);
                });
        log.debug("Successfully retrieved user with ID: {}, username: {}", id, user.getUsername());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        User user = userService.getCurrentUserEntity();
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        log.debug("Found {} users in the database", users.size());
        return users.stream()
                .map(user -> {
                    log.trace("Mapping user to DTO: {}", user.getUsername());
                    return userMapper.toDto(user);
                })
                .toList();
    }

    /**
     * Checks if the username is valid and available for update.
     * @param existingUser The existing user being updated
     * @param newUsername The new username to check
     * @throws AlreadyExistsException if the username is already taken
     */
    private void validateUsernameUpdate(User existingUser, String newUsername) {
        if (newUsername != null && !newUsername.equals(existingUser.getUsername())) {
            log.debug("Checking if username '{}' is available", newUsername);
            if (userRepository.existsByUsername(newUsername)) {
                log.warn("Username '{}' is already taken", newUsername);
                throw new AlreadyExistsException("Username already exists");
            }
            log.debug("Username '{}' is available", newUsername);
        }
    }

    /**
     * Checks if the email is valid and available for update.
     * @param existingUser The existing user being updated
     * @param newEmail The new email to check
     * @throws AlreadyExistsException if the email is already in use
     */
    private void validateEmailUpdate(User existingUser, String newEmail) {
        if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
            log.debug("Checking if email '{}' is available", newEmail);
            if (userRepository.existsByEmail(newEmail)) {
                log.warn("Email '{}' is already in use", newEmail);
                throw new AlreadyExistsException("Email already in use");
            }
            log.debug("Email '{}' is available", newEmail);
        }
    }

    /**
     * Updates the user's password if a new one is provided.
     * @param user The user to update
     * @param newPassword The new password (can be null)
     * @return true if the password was changed, false otherwise
     */
    private boolean updateUserPassword(User user, String newPassword) {
        if (newPassword != null) {
            log.debug("Updating password for user ID: {}", user.getId());
            user.setPassword(passwordEncoder.encode(newPassword));
            return true;
        }
        return false;
    }

    /**
     * Updates the user's enabled status if it has changed.
     * @param user The user to update
     * @param newEnabledStatus The new enabled status (can be null)
     * @return true if the enabled status was changed, false otherwise
     */
    private boolean updateUserEnabledStatus(User user, Boolean newEnabledStatus) {
        if (newEnabledStatus != null && !newEnabledStatus.equals(user.isEnabled())) {
            log.debug("Setting enabled status to {} for user ID: {}", newEnabledStatus, user.getId());
            user.setEnabled(newEnabledStatus);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest updateUserRequest) {
        log.info("Starting to update user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for update with ID: {}", id);
                    return new NotFoundException(USER_NOT_FOUND_MESSAGE + id);
                });
        
        log.debug("Current user data before update - Username: {}, Email: {}, Role: {}, Enabled: {}", 
                existingUser.getUsername(), existingUser.getEmail(), 
                existingUser.getRole(), existingUser.isEnabled());
        
        validateUsernameUpdate(existingUser, updateUserRequest.getUsername());
        validateEmailUpdate(existingUser, updateUserRequest.getEmail());
        
        // Update user fields from DTO
        log.debug("Updating user fields from DTO");
        userMapper.updateUserFromDto(updateUserRequest, existingUser);
        
        // Track changes for event publishing
        boolean passwordChanged = updateUserPassword(existingUser, updateUserRequest.getPassword());
        boolean enabledStatusChanged = updateUserEnabledStatus(existingUser, updateUserRequest.getEnabled());
        
        User updatedUser = userRepository.save(existingUser);
        logUserUpdateCompletion(updatedUser, id);
        
        // Publish appropriate events
        try {
            userEventProducer.publishUserEvent(EventType.USER_UPDATED, updatedUser);
            
            // Publish additional events if needed
            if (passwordChanged) {
                userEventProducer.publishUserEvent(EventType.USER_PASSWORD_CHANGED, updatedUser);
            }
            if (enabledStatusChanged) {
                EventType eventType = updatedUser.isEnabled() ? 
                        EventType.USER_ACCOUNT_ENABLED : EventType.USER_ACCOUNT_DISABLED;
                userEventProducer.publishUserEvent(eventType, updatedUser);
            }
        } catch (Exception e) {
            log.error("Failed to publish user update events for user {}", updatedUser.getUsername(), e);
            // Don't fail the operation if event publishing fails
        }
        
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) throws NotFoundException {
        log.info("Attempting to delete user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for deletion with ID: {}", id);
                    return new NotFoundException(USER_NOT_FOUND_MESSAGE + id);
                });
        
        log.debug("Deleting user - ID: {}, Username: {}, Role: {}", 
                user.getId(), user.getUsername(), user.getRole());
        
        userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        log.trace("Getting current user entity from security context");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found in security context");
            throw new SecurityException("No authenticated user found");
        }
        
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        log.debug("Looking up user in database with username: {}", username);
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Current user '{}' not found in database", username);
                    return new NotFoundException("Current user not found");
                });
    }
}
