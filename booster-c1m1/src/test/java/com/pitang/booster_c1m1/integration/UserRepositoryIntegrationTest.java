package com.pitang.booster_c1m1.integration;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.pitang.booster_c1m1.config.BaseIntegrationTest;
import com.pitang.booster_c1m1.domain.User;
import com.pitang.booster_c1m1.repository.UserRepository;

@DataJpaTest
@DisplayName("UserRepository Integration Tests")
public class UserRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

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

        anotherUser = User.builder()
                .name("Maria Santos")
                .email("maria@example.com")
                .password("password456")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("Should save user when valid user is provided")
    void save_SavesUser_WhenValidUserProvided() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("João Silva");
        assertThat(savedUser.getEmail()).isEqualTo("joao@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Should find user by ID when user exists")
    void findById_ReturnsUser_WhenUserExists() {
        User savedUser = userRepository.save(testUser);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("João Silva");
        assertThat(foundUser.get().getEmail()).isEqualTo("joao@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void findById_ReturnsEmpty_WhenUserNotFound() {
        Optional<User> foundUser = userRepository.findById(999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find all users with pagination")
    void findAll_ReturnsPagedUsers_WhenUsersExist() {
        userRepository.saveAll(List.of(testUser, anotherUser));
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> usersPage = userRepository.findAll(pageable);

        assertThat(usersPage.getContent()).hasSize(2);
        assertThat(usersPage.getTotalElements()).isEqualTo(2);
        assertThat(usersPage.getTotalPages()).isEqualTo(1);
        assertThat(usersPage.getNumber()).isEqualTo(0);
        assertThat(usersPage.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return empty page when no users exist")
    void findAll_ReturnsEmptyPage_WhenNoUsersExist() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> usersPage = userRepository.findAll(pageable);

        assertThat(usersPage.getContent()).isEmpty();
        assertThat(usersPage.getTotalElements()).isEqualTo(0);
        assertThat(usersPage.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should check if email exists when email is present")
    void existsByEmail_ReturnsTrue_WhenEmailExists() {
        userRepository.save(testUser);

        boolean exists = userRepository.existsByEmail("joao@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should check if email exists when email is not present")
    void existsByEmail_ReturnsFalse_WhenEmailNotExists() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists for different user when email exists for another user")
    void existsByEmailAndIdNot_ReturnsTrue_WhenEmailExistsForAnotherUser() {
        userRepository.save(testUser);
        User savedAnotherUser = userRepository.save(anotherUser);

        boolean exists = userRepository.existsByEmailAndIdNot("joao@example.com", savedAnotherUser.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should check if email exists for different user when email belongs to same user")
    void existsByEmailAndIdNot_ReturnsFalse_WhenEmailBelongsToSameUser() {
        User savedUser = userRepository.save(testUser);

        boolean exists = userRepository.existsByEmailAndIdNot("joao@example.com", savedUser.getId());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists for different user when email does not exist")
    void existsByEmailAndIdNot_ReturnsFalse_WhenEmailNotExists() {
        User savedUser = userRepository.save(testUser);

        boolean exists = userRepository.existsByEmailAndIdNot("nonexistent@example.com", savedUser.getId());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find users by name containing ignore case when users match")
    void findByNameContainingIgnoreCase_ReturnsMatchingUsers_WhenUsersMatch() {
        User userWithSimilarName = User.builder()
                .name("João Pedro")
                .email("joao.pedro@example.com")
                .password("password789")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        userRepository.saveAll(List.of(testUser, anotherUser, userWithSimilarName));
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> usersPage = userRepository.findByNameContainingIgnoreCase("joão", pageable);

        assertThat(usersPage.getContent()).hasSize(2);
        assertThat(usersPage.getContent())
                .extracting(User::getName)
                .containsExactlyInAnyOrder("João Silva", "João Pedro");
    }

    @Test
    @DisplayName("Should find users by name containing ignore case with case insensitive search")
    void findByNameContainingIgnoreCase_ReturnsMatchingUsers_WhenCaseInsensitive() {
        userRepository.saveAll(List.of(testUser, anotherUser));
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> usersPage = userRepository.findByNameContainingIgnoreCase("JOÃO", pageable);

        assertThat(usersPage.getContent()).hasSize(1);
        assertThat(usersPage.getContent().get(0).getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Should return empty page when no users match name filter")
    void findByNameContainingIgnoreCase_ReturnsEmptyPage_WhenNoUsersMatch() {
        userRepository.saveAll(List.of(testUser, anotherUser));
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> usersPage = userRepository.findByNameContainingIgnoreCase("NonExistent", pageable);

        assertThat(usersPage.getContent()).isEmpty();
        assertThat(usersPage.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should delete user when user exists")
    void delete_DeletesUser_WhenUserExists() {
        User savedUser = userRepository.save(testUser);

        userRepository.delete(savedUser);

        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should delete user by ID when user exists")
    void deleteById_DeletesUser_WhenUserExists() {
        User savedUser = userRepository.save(testUser);

        userRepository.deleteById(savedUser.getId());

        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should count users correctly")
    void count_ReturnsCorrectCount_WhenUsersExist() {
        userRepository.saveAll(List.of(testUser, anotherUser));

        long count = userRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return zero count when no users exist")
    void count_ReturnsZero_WhenNoUsersExist() {
        long count = userRepository.count();

        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should update user when user exists")
    void save_UpdatesUser_WhenUserExists() {
        User savedUser = userRepository.save(testUser);
        
        savedUser.setName("João Silva Atualizado");
        savedUser.setEmail("joao.atualizado@example.com");
        savedUser.setUpdatedAt("2024-01-02T10:00:00Z");

        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("João Silva Atualizado");
        assertThat(updatedUser.getEmail()).isEqualTo("joao.atualizado@example.com");
        assertThat(updatedUser.getUpdatedAt()).isEqualTo("2024-01-02T10:00:00Z");
    }
}