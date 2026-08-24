package com.abi.coding_tracker.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abi.coding_tracker.analytics.dto.StreakResponse;
import com.abi.coding_tracker.codeforces.repository.CodeForcesProfileRepository;
import com.abi.coding_tracker.codeforces.repository.CodeforcesSnapshotRepository;
import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.leetcode.entity.LeetcodeProfile;
import com.abi.coding_tracker.leetcode.entity.LeetcodeStatsSnapshot;
import com.abi.coding_tracker.leetcode.repository.LeetcodeProfileRespository;
import com.abi.coding_tracker.leetcode.repository.LeetcodeStatsSnapshotsRepository;
import com.abi.coding_tracker.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    
    @Mock private UserRepository userRepository;
    @Mock private LeetcodeProfileRespository leetcodeProfileRespository;
    @Mock private LeetcodeStatsSnapshotsRepository leetcodeStatsSnapshotsRepository;
    @Mock private CodeForcesProfileRepository codeForcesProfileRepository;
    @Mock private CodeforcesSnapshotRepository codeforcesSnapshotRepository;

    @InjectMocks private AnalyticsService analyticsService;

    @Test
    void shouldCalculateStreakCorrectlyWhenConsecutiveDaysExist() {
        // Arrange
        User user = new User();
        user.setEmail("streak@test.com");

        LeetcodeProfile profile = new LeetcodeProfile();
        profile.setUsername("streakUser");

        // Mock 4 days of snapshots. 
        // Day 1: 10 solved
        // Day 2: 12 solved (+2)
        // Day 3: 15 solved (+3)
        // Day 4 (Today): 16 solved (+1)
        LocalDateTime today = LocalDateTime.now();
        
        LeetcodeStatsSnapshot snap1 = new LeetcodeStatsSnapshot();
        snap1.setFetchedAt(today.minusDays(3));
        snap1.setTotalSolved(10);

        LeetcodeStatsSnapshot snap2 = new LeetcodeStatsSnapshot();
        snap2.setFetchedAt(today.minusDays(2));
        snap2.setTotalSolved(12);

        LeetcodeStatsSnapshot snap3 = new LeetcodeStatsSnapshot();
        snap3.setFetchedAt(today.minusDays(1));
        snap3.setTotalSolved(15);

        LeetcodeStatsSnapshot snap4 = new LeetcodeStatsSnapshot();
        snap4.setFetchedAt(today);
        snap4.setTotalSolved(16);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(leetcodeProfileRespository.findByUser(any())).thenReturn(Optional.of(profile));
        
        // Return them in ascending order like the actual database would
        when(leetcodeStatsSnapshotsRepository.findAllByProfileOrderByFetchedAtAsc(any()))
                .thenReturn(Arrays.asList(snap1, snap2, snap3, snap4));

        // Act
        StreakResponse streak = analyticsService.getStreak("streak@test.com");

        // Assert
        // The streak should be 3, because the user solved problems consecutively 
        // leading up to today (Day2, Day3, Day4).
        assertEquals(3, streak.getCurrentStreak());
    }
}
