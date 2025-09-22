package com.pitang.booster_c1m1.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
import org.springframework.web.server.ResponseStatusException;

import com.pitang.booster_c1m1.domain.User;
import com.pitang.booster_c1m1.dto.CreateUserDTO;
import com.pitang.booster_c1m1.dto.UserDTO;
import com.pitang.booster_c1m1.repository.UserRepository;

import io.micrometer.core.instrument.Counter;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private Counter userCreatedCounter;

  @Mock
  private Counter userUpdatedCounter;

  @Mock
  private Counter userDeletedCounter;

  @Mock
  private Counter userNotFoundCounter;

  @Mock
  private Counter emailConflictCounter;

  @InjectMocks
  private UserService userService;

  private User user;
  private User anotherUser;
  private CreateUserDTO createUserDTO;
  private CreateUserDTO anotherCreateUserDTO;
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

    createUserDTO = CreateUserDTO.builder()
        .name("João Silva")
        .email("joao@email.com")
        .password("password123")
        .build();

    anotherUser = User.builder()
        .id(2L)
        .name("Maria Souza")
        .email("maria@email.com")
        .createdAt(Instant.now().toString())
        .updatedAt(Instant.now().toString())
        .build();

    anotherCreateUserDTO = CreateUserDTO.builder()
        .name("Maria Souza")
        .email("maria@email.com")
        .password("password456")
        .build();
  }

  @Test
  @DisplayName("Should get all users paged when successful")
  void getAllUsers_ReturnsAllUsers_WhenSuccesful() {
    List<User> users = Arrays.asList(user, anotherUser);
    Page<User> userPage = new PageImpl<>(users);

    when(userRepository.findAll(pageable)).thenReturn(userPage);

    Page<UserDTO> result = userService.getAllUsers(pageable, null);

    assertThat(result).hasSize(2);
    assertThat(result.getContent().get(0).getName()).isEqualTo("João Silva");
    assertThat(result.getContent().get(1).getName()).isEqualTo("Maria Souza");
    verify(userRepository).findAll(pageable);
    verify(userRepository, never()).findByNameContainingIgnoreCase(anyString(), any());
  }

  @Test
  @DisplayName("Should get all users filtered by name when successful")
  void getAllUsers_WithNameFilter_ShouldReturnFilteredUsers() {
    List<User> users = Arrays.asList(user);
    Page<User> userPage = new PageImpl<>(users);

    when(userRepository.findByNameContainingIgnoreCase("João", pageable)).thenReturn(userPage);

    Page<UserDTO> result = userService.getAllUsers(pageable, "João");

    assertThat(result).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("João Silva");
    verify(userRepository).findByNameContainingIgnoreCase("João", pageable);
    verify(userRepository, never()).findAll(any(Pageable.class));
  }

  @Test
  @DisplayName("Should return empty page when no users found")
  void getAllUsers_ReturnsEmptyPage_WhenNoUsersFound() {
    Page<User> userPage = new PageImpl<>(List.of());

    when(userRepository.findAll(pageable)).thenReturn(userPage);

    Page<UserDTO> result = userService.getAllUsers(pageable, null);

    assertThat(result).isEmpty();
    verify(userRepository).findAll(pageable);
    verify(userRepository, never()).findByNameContainingIgnoreCase(anyString(), any());
  }

  @Test
  @DisplayName("Should return empty user list if name filter yields no results")
  void getAllUsers_WithNameFilter_ReturnsEmptyList_WhenNoUsersFound() {
    Page<User> userPage = new PageImpl<>(List.of());
    when(userRepository.findByNameContainingIgnoreCase("NonExistent", pageable)).thenReturn(userPage);
    Page<UserDTO> result = userService.getAllUsers(pageable, "NonExistent");
    assertThat(result).isEmpty();
    verify(userRepository).findByNameContainingIgnoreCase("NonExistent", pageable);
    verify(userRepository, never()).findAll(any(Pageable.class));
  }

  @Test
  @DisplayName("Should find user by id when successful")
  void findUserById_ReturnsUser_WhenSuccesful() {
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    UserDTO result = userService.getUserById(1L);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("João Silva");
    verify(userRepository).findById(1L);
    // Verify metrics counter is NOT incremented for successful find
    verify(userNotFoundCounter, never()).increment();
  }

  @Test
  @DisplayName("Should throw not found exception when user id does not exist")
  void findUserById_ThrowsNotFoundException_WhenUserDoesNotExist() {
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.empty());
    assertThatThrownBy(() -> userService.getUserById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User not found");
    verify(userRepository).findById(1L);
    verify(userNotFoundCounter).increment();
  }

  @Test
  @DisplayName("Should create user when successful")
  void createUser_CreatesUser_WhenSuccesful() {
    when(userRepository.save(any(User.class))).thenReturn(user);
    UserDTO result = userService.createUser(createUserDTO);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("João Silva");
    assertThat(result.getEmail()).isEqualTo("joao@email.com");
    verify(userRepository).save(any(User.class));
    verify(userCreatedCounter).increment();
    verify(emailConflictCounter, never()).increment();
  }

  @Test
  @DisplayName("Should throw conflict exception when creating user with existing email")
  void createUser_ThrowsConflictException_WhenEmailAlreadyExists() {
    when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);
    assertThatThrownBy(() -> userService.createUser(createUserDTO))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Email already in use");
    verify(userRepository).existsByEmail("joao@email.com");
    verify(userRepository, never()).save(any(User.class));
    verify(emailConflictCounter).increment();
    verify(userCreatedCounter, never()).increment();
  }

  @Test
  @DisplayName("Should update user when successful")
  void updateUser_UpdatesUser_WhenSuccesful() {
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(userRepository.existsByEmailAndIdNot("joao.silva@email.com", 1L)).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(user);
    createUserDTO.setEmail("joao.silva@email.com");
    UserDTO result = userService.updateUser(1L, createUserDTO);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("João Silva");
    assertThat(result.getEmail()).isEqualTo("joao.silva@email.com");
    verify(userRepository).findById(1L);
    verify(userRepository).existsByEmailAndIdNot("joao.silva@email.com", 1L);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw not found exception when updating non-existent user")
  void updateUser_ThrowsNotFoundException_WhenUserDoesNotExist() {
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.empty());
    assertThatThrownBy(() -> userService.updateUser(1L, createUserDTO))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User not found");
    verify(userRepository).findById(1L);
    verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any(Long.class));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw conflict exception when updating user with existing email")
  void updateUser_ThrowsConflictException_WhenEmailAlreadyExists() {
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(userRepository.existsByEmailAndIdNot("maria@email.com", 1L)).thenReturn(true);
    assertThatThrownBy(() -> userService.updateUser(1L, anotherCreateUserDTO))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Email already in use");
    verify(userRepository).findById(1L);
    verify(userRepository).existsByEmailAndIdNot("maria@email.com", 1L);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should delete user when successful")
  void deleteUser_DeletesUser_WhenSuccessful() {
    when(userRepository.existsById(1L)).thenReturn(true);
    userService.deleteUser(1L);
    verify(userRepository).deleteById(1L);
    verify(userDeletedCounter).increment();
    verify(userNotFoundCounter, never()).increment();
  }

  @Test
  @DisplayName("Should throw not found exception when deleting non-existent user")
  void deleteUser_ThrowsNotFoundException_WhenUserDoesNotExist() {
    when(userRepository.existsById(1L)).thenReturn(false);
    assertThatThrownBy(() -> userService.deleteUser(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User not found");
    verify(userRepository).existsById(1L);
    verify(userRepository, never()).deleteById(any(Long.class));
    verify(userNotFoundCounter).increment();
    verify(userDeletedCounter, never()).increment();
  }

  @Test
  @DisplayName("Should throw bad request exception when deleting user with invalid id")
  void deleteUser_ThrowsBadRequestException_WhenIdIsInvalid() {
    assertThatThrownBy(() -> userService.deleteUser(0L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Invalid user ID");
    verify(userRepository, never()).existsById(any(Long.class));
    verify(userRepository, never()).deleteById(any(Long.class));
    verify(userDeletedCounter, never()).increment();
    verify(userNotFoundCounter, never()).increment();
  }
}
