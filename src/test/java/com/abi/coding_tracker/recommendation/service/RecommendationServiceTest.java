package com.abi.coding_tracker.recommendation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abi.coding_tracker.analytics.dto.AnalyticsSummaryResponse;
import com.abi.coding_tracker.analytics.dto.DifficultyDistributionResponse;
import com.abi.coding_tracker.analytics.service.AnalyticsService;
import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.github.repository.GithubProfileRepository;
import com.abi.coding_tracker.github.repository.GithubSnapshotRepository;
import com.abi.coding_tracker.recommendation.dto.RecommendationResponse;
import com.abi.coding_tracker.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    @Mock private AnalyticsService analyticsService;
    @Mock private UserRepository userRepository;
    @Mock private GithubProfileRepository githubProfileRepository;
    @Mock private GithubSnapshotRepository githubSnapshotRepository;

    @InjectMocks private RecommendationService recommendationService;

    private User dummyUser;

    @BeforeEach
    void setUp(){
        dummyUser = new User();
        dummyUser.setEmail("test@gmail.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(dummyUser));
    }

    @Test
    void shouldRecommendConnectingAccountsWhenNothingIsConnected(){
        when(analyticsService.getAnalyticsSummary(anyString())).thenReturn(new AnalyticsSummaryResponse(0,0,null,0,0));
        when(analyticsService.getDifficultyDistribution(anyString())).thenReturn(new DifficultyDistributionResponse(0,0,0));
        when(githubProfileRepository.findByUser(any())).thenReturn(Optional.empty());

        RecommendationResponse response = recommendationService.generateRecommendation("test@gmail.com");

        assertEquals("Beginner", response.getOverallLevel());
        assertTrue(response.getRecommendations().get(0).contains("Start by connecting your coding accounts"));
    }
    @Test
    void shouldRecommendHardProblemsWhenMediumIsHighAndHardIsLow() {
        // Arrange
        when(analyticsService.getAnalyticsSummary(anyString())).thenReturn(new AnalyticsSummaryResponse(200, 5, 1500, 10, 0)); // Solved > 0 so it bypasses the "no accounts" check
        when(analyticsService.getDifficultyDistribution(anyString())).thenReturn(new DifficultyDistributionResponse(50, 100, 10)); // High Medium, Low Hard
        when(githubProfileRepository.findByUser(any())).thenReturn(Optional.empty());

        // Act
        RecommendationResponse response = recommendationService.generateRecommendation("test@gmail.com");

        // Assert
        boolean containsHardProblemAdvice = response.getRecommendations().stream()
                .anyMatch(rec -> rec.contains("focus on Hard problems"));
        assertTrue(containsHardProblemAdvice);
    }
}
