package com.pitang.booster_c1m1.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pitang.booster_c1m1.domain.User;
import com.pitang.booster_c1m1.dto.CreateUserDTO;
import com.pitang.booster_c1m1.dto.UserDTO;
import com.pitang.booster_c1m1.mapper.UserMapper;
import com.pitang.booster_c1m1.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private static final UserMapper MAPPER = UserMapper.INSTANCE;

    private final UserRepository userRepository;

    public Page<UserDTO> getAllUsers(Pageable pageable, String name) {
        log.debug("Fetching users from database - name filter: {}", name);
        Page<User> users;
        if (name != null) {
            users = userRepository.findByNameContainingIgnoreCase(name, pageable);
            log.debug("Found {} users matching name '{}'", users.getTotalElements(), name);
        } else {
            users = userRepository.findAll(pageable);
            log.debug("Found {} total users", users.getTotalElements());
        }
        return users.map(MAPPER::toUserDTO);
    }

    public UserDTO getUserById(Long id) {
        log.debug("Searching for user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        log.debug("User found: {}", user.getEmail());
        return MAPPER.toUserDTO(user);
    }

    public UserDTO createUser(CreateUserDTO createUserDTO) {
        log.debug("Attempting to create user with email: {}", createUserDTO.getEmail());
        User user = MAPPER.toUser(createUserDTO);

        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Attempt to create user with existing email: {}", user.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        user.setCreatedAt(Instant.now().toString());
        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        return MAPPER.toUserDTO(savedUser);
    }

    public void deleteUser(long id) {
        log.debug("Attempting to delete user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Attempt to delete non-existent user with id: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        log.info("User with id {} deleted successfully", id);
    }
}