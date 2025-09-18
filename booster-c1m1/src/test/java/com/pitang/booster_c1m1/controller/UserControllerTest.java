package com.pitang.booster_c1m1.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
public class UserControllerTest {

  @Mock
  private UserService userService;

  @Mock
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

  @Test
  @DisplayName("Should create user when successful")
  void createUser_CreatesUser_WhenSuccessful() {
    CreateUserDTO createUserDTO = new CreateUserDTO();
    createUserDTO.setName("New User");
    createUserDTO.setEmail("newuser@email.com");
    createUserDTO.setPassword("password123");
    User createdUser = User.builder()
        .id(3L)
        .name(createUserDTO.getName())
        .email(createUserDTO.getEmail())
        .createdAt(Instant.now().toString())
        .updatedAt(Instant.now().toString())
        .build();
    UserDTO createdUserDTO = userMapper.toDto(createdUser);

    when(userService.createUser(createUserDTO)).thenReturn(createdUserDTO);

    ResponseEntity<UserDTO> response = userController.createUser(createUserDTO);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEqualTo(createdUserDTO);
  }
}
