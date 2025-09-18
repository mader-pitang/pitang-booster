package com.pitang.booster_c1m1.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.pitang.booster_c1m1.domain.User;
import com.pitang.booster_c1m1.dto.CreateUserDTO;
import com.pitang.booster_c1m1.dto.PaginatedResponseDTO;
import com.pitang.booster_c1m1.dto.UserDTO;
import com.pitang.booster_c1m1.mapper.UserMapper;
import com.pitang.booster_c1m1.service.UserService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
public class UserControllerTest {

  @Mock
  private UserService userService;

  private UserMapper userMapper = UserMapper.INSTANCE;

  @InjectMocks
  private UserController userController;

  private User user;
  private User anotherUser;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    pageable = PageRequest.of(0, 10);
    user = User.builder()
        .id(1L)
        .name("João Silva")
        .email("joao@email.com")
        .createdAt(Instant.now().toString())
        .updatedAt(Instant.now().toString())
        .build();

    anotherUser = User.builder()
        .id(2L)
        .name("Maria Souza")
        .email("maria@email.com")
        .createdAt(Instant.now().toString())
        .updatedAt(Instant.now().toString())
        .build();

  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return all users paged when successful")
  void getAllUsers_ReturnsPagedUsers_WhenSuccessful() {
    List<UserDTO> userDTOs = Arrays.asList(userMapper.toDto(user), userMapper.toDto(anotherUser));
    Page<UserDTO> userPage = new PageImpl<>(userDTOs, pageable, userDTOs.size());

    when(userService.getAllUsers(pageable, null)).thenReturn(userPage);

    ResponseEntity<PaginatedResponseDTO<UserDTO>> response = userController.getAllUsers(pageable, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(2);
    assertThat(response.getBody().getContent()).containsExactlyElementsOf(userDTOs);
    assertThat(response.getBody().getPage()).isEqualTo(0);
    assertThat(response.getBody().getSize()).isEqualTo(10);

    verify(userService).getAllUsers(pageable, null);
  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty list when no users are found")
  void getAllUsers_ReturnsEmptyList_WhenNoUsersFound() {
    Page<UserDTO> emptyPage = Page.empty(pageable);
    when(userService.getAllUsers(pageable, null)).thenReturn(emptyPage);

    ResponseEntity<PaginatedResponseDTO<UserDTO>> response = userController.getAllUsers(pageable, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).isEmpty();
    assertThat(response.getBody().getPage()).isEqualTo(0);
    assertThat(response.getBody().getSize()).isEqualTo(10);
    verify(userService).getAllUsers(pageable, null);

  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return filtered users when name parameter is provided")
  void getAllUsers_ReturnsFilteredUsers_WhenNameParameterProvided() {
    String nameFilter = "João";
    List<UserDTO> userDTOs = Arrays.asList(userMapper.toDto(user));
    Page<UserDTO> userPage = new PageImpl<>(userDTOs, pageable, userDTOs.size());
    when(userService.getAllUsers(pageable, nameFilter)).thenReturn(userPage);

    ResponseEntity<PaginatedResponseDTO<UserDTO>> response = userController.getAllUsers(pageable, nameFilter);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(1);
    assertThat(response.getBody().getContent()).containsExactlyElementsOf(userDTOs);
    assertThat(response.getBody().getPage()).isEqualTo(0);
    assertThat(response.getBody().getSize()).isEqualTo(10);
    verify(userService).getAllUsers(pageable, nameFilter);
  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty list when no users match the name filter")
  void getAllUsers_ReturnsEmptyList_WhenNoUsersMatchNameFilter() {
    String nameFilter = "NonExistentName";
    Page<UserDTO> emptyPage = Page.empty(pageable);
    when(userService.getAllUsers(pageable, nameFilter)).thenReturn(emptyPage);

    ResponseEntity<PaginatedResponseDTO<UserDTO>> response = userController.getAllUsers(pageable, nameFilter);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).isEmpty();
    assertThat(response.getBody().getPage()).isEqualTo(0);
    assertThat(response.getBody().getSize()).isEqualTo(10);
    verify(userService).getAllUsers(pageable, nameFilter);
  }

  @Test
  @DisplayName("Should return user by ID when successful")
  void getUserById_ReturnsUser_WhenSuccessful() {
    Long userId = 1L;
    UserDTO userDTO = userMapper.toDto(user);
    when(userService.getUserById(userId)).thenReturn(userDTO);

    ResponseEntity<UserDTO> response = userController.getUserById(userId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEqualTo(userDTO);
    verify(userService).getUserById(userId);
  }

  @Test
  @DisplayName("Should return 404 when user not found by ID")
  void getUserById_Returns404_WhenUserNotFound() {
    Long userId = 999L;
    when(userService.getUserById(userId))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    assertThatThrownBy(() -> userController.getUserById(userId))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
        .hasMessageContaining("User not found");
    verify(userService).getUserById(userId);
  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should create user when successful")
  void createUser_CreatesUser_WhenSuccessful() {
    CreateUserDTO createUserDTO = CreateUserDTO.builder()
        .name("New User")
        .email("newuser@email.com")
        .password("password123")
        .build();

    UserDTO createdUserDTO = new UserDTO();
    createdUserDTO.setId(1L);
    createdUserDTO.setName("New User");
    createdUserDTO.setEmail("newuser@email.com");
    createdUserDTO.setCreatedAt(Instant.now().toString());
    createdUserDTO.setUpdatedAt(Instant.now().toString());

    when(userService.createUser(createUserDTO)).thenReturn(createdUserDTO);

    ResponseEntity<UserDTO> response = userController.createUser(createUserDTO);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getName()).isEqualTo("New User");
    assertThat(response.getBody().getEmail()).isEqualTo("newuser@email.com");
    assertThat(response.getBody().getId()).isNotNull();
    verify(userService).createUser(createUserDTO);
  }

  @Test
  @DisplayName("Should update user when successful")
  void updateUser_UpdatesUser_WhenSuccessful() {
    Long userId = 1L;
    CreateUserDTO updateDTO = CreateUserDTO.builder()
        .name("Updated Name")
        .email("updated@email.com")
        .password("newpass")
        .build();

    UserDTO updatedUserDTO = new UserDTO();
    updatedUserDTO.setId(userId);
    updatedUserDTO.setName("Updated Name");
    updatedUserDTO.setEmail("updated@email.com");
    updatedUserDTO.setCreatedAt(user.getCreatedAt());
    updatedUserDTO.setUpdatedAt(Instant.now().toString());

    when(userService.updateUser(userId, updateDTO)).thenReturn(updatedUserDTO);

    ResponseEntity<UserDTO> response = userController.updateUser(userId, updateDTO);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEqualTo(updatedUserDTO);
    verify(userService).updateUser(userId, updateDTO);
  }

  @Test
  @DisplayName("Should return 409 when updating with an email already in use")
  void updateUser_Returns409_WhenEmailConflict() {
    Long userId = 1L;
    CreateUserDTO updateDTO = CreateUserDTO.builder()
        .name("Another")
        .email("existing@email.com")
        .password("pass")
        .build();

    when(userService.updateUser(userId, updateDTO))
        .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use"));

    assertThatThrownBy(() -> userController.updateUser(userId, updateDTO))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
        .hasMessageContaining("Email already in use");

    verify(userService).updateUser(userId, updateDTO);
  }

  @Test
  @DisplayName("Should return 404 when updating non-existent user")
  void updateUser_Returns404_WhenUserNotFound() {
    Long userId = 999L;
    CreateUserDTO updateDTO = CreateUserDTO.builder()
        .name("No One")
        .email("noone@email.com")
        .password("pass")
        .build();

    when(userService.updateUser(userId, updateDTO))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    assertThatThrownBy(() -> userController.updateUser(userId, updateDTO))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
        .hasMessageContaining("User not found");

    verify(userService).updateUser(userId, updateDTO);
  }

  @Test
  @DisplayName("Should delete user when successful")
  void deleteUser_DeletesUser_WhenSuccessful() {
    Long userId = 1L;

    ResponseEntity<Void> response = userController.deleteUser(userId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
    verify(userService).deleteUser(userId);
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent user")
  void deleteUser_Returns404_WhenUserNotFound() {
    Long userId = 999L;
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")).when(userService).deleteUser(userId);

    assertThatThrownBy(() -> userController.deleteUser(userId))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
        .hasMessageContaining("User not found");

    verify(userService).deleteUser(userId);
  }

  @Test
  @DisplayName("Should return 400 when deleting with invalid id")
  void deleteUser_Returns400_WhenInvalidId() {
    Long invalidId = 0L;
    doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID")).when(userService)
        .deleteUser(invalidId);

    assertThatThrownBy(() -> userController.deleteUser(invalidId))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
        .hasMessageContaining("Invalid user ID");

    verify(userService).deleteUser(invalidId);
  }

  @Test
  @DisplayName("Should fail validation when update DTO is invalid")
  void updateUser_Returns400_WhenInvalidInput() {
    CreateUserDTO invalidDTO = CreateUserDTO.builder()
        .name("")
        .email("invalid-email")
        .password("123")
        .build();

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(invalidDTO);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .anyMatch(v -> v.getMessage().contains("Name is required") || v.getPropertyPath().toString().equals("name"));
    assertThat(violations).anyMatch(
        v -> v.getMessage().contains("Email should be valid") || v.getPropertyPath().toString().equals("email"));
    assertThat(violations).anyMatch(v -> v.getMessage().contains("Password must be at least 6 characters long")
        || v.getPropertyPath().toString().equals("password"));

    verify(userService, org.mockito.Mockito.never()).updateUser(org.mockito.Mockito.anyLong(),
        org.mockito.Mockito.any());
  }
}
