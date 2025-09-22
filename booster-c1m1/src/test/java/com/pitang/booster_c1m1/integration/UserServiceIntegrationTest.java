package com.pitang.booster_c1m1.integration;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pitang.booster_c1m1.domain.User;
import com.pitang.booster_c1m1.dto.CreateUserDTO;
import com.pitang.booster_c1m1.dto.UserDTO;
import com.pitang.booster_c1m1.repository.UserRepository;
import com.pitang.booster_c1m1.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService Integration Tests")
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private CreateUserDTO createUserDTO;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .name("João Silva")
                .email("joao@example.com")
                .password("password123")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        createUserDTO = CreateUserDTO.builder()
                .name("Maria Santos")
                .email("maria@example.com")
                .password("password456")
                .build();
    }

    @Test
    @DisplayName("Should get all users with pagination when users exist")
    void getAllUsers_ReturnsPagedUsers_WhenUsersExist() {
        User anotherUser = User.builder()
                .name("Pedro Costa")
                .email("pedro@example.com")
                .password("password789")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        userRepository.saveAll(List.of(testUser, anotherUser));
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserDTO> result = userService.getAllUsers(pageable, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(UserDTO::getName)
                .containsExactlyInAnyOrder("João Silva", "Pedro Costa");
    }

    @Test
    @DisplayName("Should get filtered users when name parameter is provided")
    void getAllUsers_ReturnsFilteredUsers_WhenNameParameterProvided() {
        User anotherUser = User.builder()
                .name("Pedro Costa")
                .email("pedro@example.com")
                .password("password789")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        userRepository.saveAll(List.of(testUser, anotherUser));
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserDTO> result = userService.getAllUsers(pageable, "João");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Should return empty page when no users exist")
    void getAllUsers_ReturnsEmptyPage_WhenNoUsersExist() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserDTO> result = userService.getAllUsers(pageable, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should get user by ID when user exists")
    void getUserById_ReturnsUser_WhenUserExists() {
        User savedUser = userRepository.save(testUser);

        UserDTO result = userService.getUserById(savedUser.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedUser.getId());
        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getEmail()).isEqualTo("joao@example.com");
    }

    @Test
    @DisplayName("Should throw not found exception when user does not exist")
    void getUserById_ThrowsNotFoundException_WhenUserNotExists() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("Should create user when valid data is provided")
    void createUser_CreatesUser_WhenValidDataProvided() {
        UserDTO result = userService.createUser(createUserDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Santos");
        assertThat(result.getEmail()).isEqualTo("maria@example.com");

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Maria Santos");
        assertThat(users.get(0).getEmail()).isEqualTo("maria@example.com");
    }

    @Test
    @DisplayName("Should throw conflict exception when creating user with existing email")
    void createUser_ThrowsConflictException_WhenEmailAlreadyExists() {
        userRepository.save(testUser);

        CreateUserDTO duplicateEmailDTO = CreateUserDTO.builder()
                .name("Outro Nome")
                .email("joao@example.com")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userService.createUser(duplicateEmailDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    @DisplayName("Should update user when valid data is provided")
    void updateUser_UpdatesUser_WhenValidDataProvided() {
        User savedUser = userRepository.save(testUser);

        CreateUserDTO updateDTO = CreateUserDTO.builder()
                .name("João Silva Atualizado")
                .email("joao.atualizado@example.com")
                .password("newpassword123")
                .build();

        UserDTO result = userService.updateUser(savedUser.getId(), updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedUser.getId());
        assertThat(result.getName()).isEqualTo("João Silva Atualizado");
        assertThat(result.getEmail()).isEqualTo("joao.atualizado@example.com");

        User updatedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("João Silva Atualizado");
        assertThat(updatedUser.getEmail()).isEqualTo("joao.atualizado@example.com");
    }

    @Test
    @DisplayName("Should throw not found exception when updating non-existent user")
    void updateUser_ThrowsNotFoundException_WhenUserNotExists() {
        assertThatThrownBy(() -> userService.updateUser(999L, createUserDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("Should throw conflict exception when updating user with existing email")
    void updateUser_ThrowsConflictException_WhenEmailAlreadyExists() {
        User anotherUser = User.builder()
                .name("Pedro Costa")
                .email("pedro@example.com")
                .password("password789")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        User savedUser = userRepository.save(testUser);
        userRepository.save(anotherUser);

        CreateUserDTO updateDTO = CreateUserDTO.builder()
                .name("João Silva Atualizado")
                .email("pedro@example.com")
                .password("newpassword123")
                .build();

        assertThatThrownBy(() -> userService.updateUser(savedUser.getId(), updateDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    @DisplayName("Should delete user when user exists")
    void deleteUser_DeletesUser_WhenUserExists() {
        User savedUser = userRepository.save(testUser);

        userService.deleteUser(savedUser.getId());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should throw not found exception when deleting non-existent user")
    void deleteUser_ThrowsNotFoundException_WhenUserNotExists() {
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("Should throw bad request exception when deleting user with invalid ID")
    void deleteUser_ThrowsBadRequestException_WhenIdIsInvalid() {
        assertThatThrownBy(() -> userService.deleteUser(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    @DisplayName("Should handle concurrent user creation with same email")
    void createUser_HandlesConcurrency_WhenSameEmailUsedSimultaneously() {
        CreateUserDTO firstUserDTO = CreateUserDTO.builder()
                .name("Primeiro Usuário")
                .email("concurrent@example.com")
                .password("password123")
                .build();

        CreateUserDTO secondUserDTO = CreateUserDTO.builder()
                .name("Segundo Usuário")
                .email("concurrent@example.com")
                .password("password456")
                .build();

        userService.createUser(firstUserDTO);

        assertThatThrownBy(() -> userService.createUser(secondUserDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Primeiro Usuário");
    }

    @Test
    @DisplayName("Should maintain data integrity during update operations")
    void updateUser_MaintainsDataIntegrity_WhenUpdatingMultipleFields() {
        User savedUser = userRepository.save(testUser);
        String originalCreatedAt = savedUser.getCreatedAt();

        CreateUserDTO updateDTO = CreateUserDTO.builder()
                .name("Nome Completamente Novo")
                .email("email.completamente.novo@example.com")
                .password("senhacompletamentenovaesegura")
                .build();

        UserDTO result = userService.updateUser(savedUser.getId(), updateDTO);

        assertThat(result.getName()).isEqualTo("Nome Completamente Novo");
        assertThat(result.getEmail()).isEqualTo("email.completamente.novo@example.com");

        User updatedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updatedUser.getUpdatedAt()).isNotEqualTo(originalCreatedAt);
        assertThat(updatedUser.getPassword()).isEqualTo("senhacompletamentenovaesegura");
    }

    @Test
    @DisplayName("Should throw conflict exception when updating user with existing email from another user")
    void updateUser_ThrowsConflictException_WhenEmailBelongsToAnotherUser() {
        User anotherUser = User.builder()
                .name("Pedro Costa")
                .email("pedro@example.com")
                .password("password789")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        User savedUser = userRepository.save(testUser);
        userRepository.save(anotherUser);

        CreateUserDTO updateDTO = CreateUserDTO.builder()
                .name("João Silva Completamente Atualizado")
                .email("pedro@example.com")
                .password("supersecurepassword")
                .build();

        assertThatThrownBy(() -> userService.updateUser(savedUser.getId(), updateDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }
}