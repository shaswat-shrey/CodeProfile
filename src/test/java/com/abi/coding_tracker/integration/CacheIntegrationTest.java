package com.abi.coding_tracker.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.leetcode.dto.LeetcodeStatsResponse;
import com.abi.coding_tracker.leetcode.entity.LeetcodeProfile;
import com.abi.coding_tracker.leetcode.repository.LeetcodeProfileRespository;
import com.abi.coding_tracker.leetcode.service.LeetcodeProfileService;
import com.abi.coding_tracker.leetcode.service.LeetcodeService;
import com.abi.coding_tracker.repository.UserRepository;

@Testcontainers
@SpringBootTest
public class CacheIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("coding_tracker_test")
            .withUsername("testuser")
            .withPassword("testpassword");

   @Container
   static GenericContainer<?> redis = new GenericContainer<>("redis:7")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private LeetcodeProfileService leetcodeProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeetcodeProfileRespository leetcodeProfileRespository;

    @MockitoBean
    private LeetcodeService leetcodeService;

    @BeforeEach
    void setup(){
        userRepository.deleteAll();

        User user = new User();
        user.setName("Cache Tester");
        user.setEmail("cache@test.com");
        user.setPassword("securepass");
        userRepository.save(user);

        LeetcodeProfile profile = new LeetcodeProfile();
        profile.setUsername("cache_coder");
        profile.setUser(user);
        leetcodeProfileRespository.save(profile);
    }

    @Test
    void shouldFetchFromApiFirstTimeAndHitCacheSecondTime() {
        // Arrange: Fake the response from LeetCode
        LeetcodeStatsResponse mockResponse = new LeetcodeStatsResponse();
        mockResponse.setTotalSolved(100);
        when(leetcodeService.fetchUserProfile(anyString())).thenReturn(mockResponse);

        // Act 1: First request should hit the "API" (our mock) and populate Redis
        leetcodeProfileService.fetchAndSaveMyStats("cache@test.com", false);
        
        // Act 2: Second request should hit Redis and NEVER reach the API mock
        leetcodeProfileService.fetchAndSaveMyStats("cache@test.com", false);

        // Assert: Verify the external API was only called exactly ONE time
        verify(leetcodeService, times(1)).fetchUserProfile("cache_coder");
    }

    @Test
    void shouldEvictCacheWhenRefreshIsForced() {
        // Arrange
        LeetcodeStatsResponse mockResponse = new LeetcodeStatsResponse();
        mockResponse.setTotalSolved(150);
        when(leetcodeService.fetchUserProfile(anyString())).thenReturn(mockResponse);

        // Act 1: Standard fetch (Populates Cache)
        leetcodeProfileService.fetchAndSaveMyStats("cache@test.com", false);

        // Act 2: Forced Refresh (Evicts/Updates Cache via @CachePut or manual logic)
        leetcodeProfileService.refreshMyStats("cache@test.com");

        // Assert: Verify the external API was called TWICE because we forced an eviction/refresh
        verify(leetcodeService, times(2)).fetchUserProfile("cache_coder");
    }
}
