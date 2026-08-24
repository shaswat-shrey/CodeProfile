package com.abi.coding_tracker.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer; // <-- Add this import
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.repository.UserRepository;

@Testcontainers
@SpringBootTest
public class UserRepositoryIntegrationTest {

    // Spin up a real PostgreSQL 16 container for testing!
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("coding_tracker_test")
            .withUsername("testuser")
            .withPassword("testpassword");

    // Add Redis because @SpringBootTest requires the caching layer to start up!
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
            .withExposedPorts(6379);

    // Dynamically inject the database and redis URLs into Spring Boot
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean up the database before each test to ensure isolation
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveUserAndFindById() {
        // Arrange
        User user = new User();
        user.setName("Alice Integration");
        user.setEmail("alice@integration.com");
        user.setPassword("securepass");

        // Act
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("Alice Integration", foundUser.get().getName());
    }

    @Test
    void shouldFindUserByEmail() {
        // Arrange
        User user = new User();
        user.setName("Bob Search");
        user.setEmail("bob@search.com");
        user.setPassword("securepass");
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("bob@search.com");
        Optional<User> missingUser = userRepository.findByEmail("nobody@search.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("Bob Search", foundUser.get().getName());
        assertTrue(missingUser.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsDuplicate() {
        // Arrange
        User user1 = new User();
        user1.setName("Charlie Original");
        user1.setEmail("charlie@duplicate.com");
        user1.setPassword("pass1");
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("Charlie Copy");
        user2.setEmail("charlie@duplicate.com"); // Exact same email
        user2.setPassword("pass2");

        // Act & Assert - Verifying actual DB constraints, not just Mockito logic!
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(user2);
        });
    }
}