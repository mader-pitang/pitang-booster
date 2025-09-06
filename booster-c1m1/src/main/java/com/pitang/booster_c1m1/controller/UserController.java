package com.pitang.booster_c1m1.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pitang.booster_c1m1.dto.CreateUserDTO;
import com.pitang.booster_c1m1.dto.PaginatedResponseDTO;
import com.pitang.booster_c1m1.dto.UserDTO;
import com.pitang.booster_c1m1.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@Validated
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<PaginatedResponseDTO<UserDTO>> getAllUsers(
      @PageableDefault(size = 10, page = 0) Pageable pageable, @RequestParam(required = false) String name) {
    log.info("getAllUsers - page: {}, size: {}, name: {}",
        pageable.getPageNumber(), pageable.getPageSize(), name);
    Page<UserDTO> users = userService.getAllUsers(pageable, name);
    log.debug("Found {} users", users.getTotalElements());
    return ResponseEntity.ok(PaginatedResponseDTO.from(users));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    log.info("getUserById - id: {}", id);
    UserDTO user = userService.getUserById(id);
    return ResponseEntity.ok(user);
  }

  @PostMapping
  public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
    log.info("createUser - email: {}", createUserDTO.getEmail());
    UserDTO user = userService.createUser(createUserDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    log.info("deleteUser - id: {}", id);
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
