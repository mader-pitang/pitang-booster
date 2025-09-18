package com.pitang.booster_c1m1.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitang.booster_c1m1.domain.User;
import com.pitang.booster_c1m1.dto.CreateUserDTO;
import com.pitang.booster_c1m1.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController Integration Tests")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Should get all users with pagination when successful")
    void getAllUsers_ReturnsPagedUsers_WhenSuccessful() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(get("/v1/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("João Silva"))
                .andExpect(jsonPath("$.content[0].email").value("joao@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @DisplayName("Should get users filtered by name when name parameter is provided")
    void getAllUsers_ReturnsFilteredUsers_WhenNameParameterProvided() throws Exception {
        User anotherUser = User.builder()
                .name("Pedro Costa")
                .email("pedro@example.com")
                .password("password789")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        userRepository.saveAll(List.of(testUser, anotherUser));

        mockMvc.perform(get("/v1/users")
                .param("name", "João")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("João Silva"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should return empty list when no users match name filter")
    void getAllUsers_ReturnsEmptyList_WhenNoUsersMatchNameFilter() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(get("/v1/users")
                .param("name", "NonExistent")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should get user by ID when user exists")
    void getUserById_ReturnsUser_WhenUserExists() throws Exception {
        User savedUser = userRepository.save(testUser);

        mockMvc.perform(get("/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@example.com"));
    }

    @Test
    @DisplayName("Should return 404 when user not found by ID")
    void getUserById_Returns404_WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create user when valid data is provided")
    void createUser_CreatesUser_WhenValidDataProvided() throws Exception {
        String requestBody = objectMapper.writeValueAsString(createUserDTO);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maria Santos"))
                .andExpect(jsonPath("$.email").value("maria@example.com"))
                .andExpect(jsonPath("$.id").exists());

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Maria Santos");
        assertThat(users.get(0).getEmail()).isEqualTo("maria@example.com");
    }

    @Test
    @DisplayName("Should return 409 when creating user with existing email")
    void createUser_Returns409_WhenEmailAlreadyExists() throws Exception {
        userRepository.save(testUser);

        CreateUserDTO duplicateEmailDTO = CreateUserDTO.builder()
                .name("Outro Nome")
                .email("joao@example.com")
                .password("password123")
                .build();

        String requestBody = objectMapper.writeValueAsString(duplicateEmailDTO);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 400 when creating user with invalid data")
    void createUser_Returns400_WhenInvalidDataProvided() throws Exception {
        CreateUserDTO invalidDTO = CreateUserDTO.builder()
                .name("")
                .email("invalid-email")
                .password("")
                .build();

        String requestBody = objectMapper.writeValueAsString(invalidDTO);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should update user when valid data is provided")
    void updateUser_UpdatesUser_WhenValidDataProvided() throws Exception {
        User savedUser = userRepository.save(testUser);

        CreateUserDTO updateDTO = CreateUserDTO.builder()
                .name("João Silva Atualizado")
                .email("joao.atualizado@example.com")
                .password("newpassword123")
                .build();

        String requestBody = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(put("/v1/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("João Silva Atualizado"))
                .andExpect(jsonPath("$.email").value("joao.atualizado@example.com"));

        User updatedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("João Silva Atualizado");
        assertThat(updatedUser.getEmail()).isEqualTo("joao.atualizado@example.com");
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent user")
    void updateUser_Returns404_WhenUserNotFound() throws Exception {
        String requestBody = objectMapper.writeValueAsString(createUserDTO);

        mockMvc.perform(put("/v1/users/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 409 when updating user with existing email")
    void updateUser_Returns409_WhenEmailAlreadyExists() throws Exception {
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
                .name("João Silva")
                .email("pedro@example.com")
                .password("password123")
                .build();

        String requestBody = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(put("/v1/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should delete user when user exists")
    void deleteUser_DeletesUser_WhenUserExists() throws Exception {
        User savedUser = userRepository.save(testUser);

        mockMvc.perform(delete("/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    void deleteUser_Returns404_WhenUserNotFound() throws Exception {
        mockMvc.perform(delete("/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when deleting user with invalid ID")
    void deleteUser_Returns400_WhenInvalidId() throws Exception {
        mockMvc.perform(delete("/v1/users/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }
}