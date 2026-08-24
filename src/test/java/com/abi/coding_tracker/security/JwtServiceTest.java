package com.abi.coding_tracker.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {
    private JwtService jwtService;

    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long EXPIRATION = 1000*60*60;

    @BeforeEach
    void setUp(){
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
    }

    @Test
    void shouldGenerateValidJwtToken(){
        String username = "testuser@gmail.com";

        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
    }

    @Test
    void shouldExtractUsernameCorrectly(){
        String expectedUsername = "abhi@test.com";
        String token = jwtService.generateToken(expectedUsername);

        // Act
        String actualUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void shouldReturnTrueWhenValidatingTokenForCorrectUser() {
        // Arrange
        String username = "validuser@gmail.com";
        String token = jwtService.generateToken(username);

        // Act
        boolean isValid = jwtService.validateToken(token, username);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseWhenValidatingTokenForWrongUser() {
        // Arrange
        String token = jwtService.generateToken("correctuser@gmail.com");

        // Act
        boolean isValid = jwtService.validateToken(token, "wronguser@gmail.com");

        // Assert
        assertFalse(isValid);
    }
}
