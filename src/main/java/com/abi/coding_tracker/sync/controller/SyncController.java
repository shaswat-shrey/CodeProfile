package com.abi.coding_tracker.sync.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.security.RateLimiterService;
import com.abi.coding_tracker.sync.dto.SyncResponse;
import com.abi.coding_tracker.sync.service.SyncService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sync")
public class SyncController {
    private final SyncService syncService;
    private final RateLimiterService rateLimiterService;

    
    public SyncController(SyncService syncService, RateLimiterService rateLimiterService){
        this.syncService= syncService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/me")
    public ResponseEntity<SyncResponse> syncMe(Authentication authentication){
        String email = authentication.getName();

        log.info("User [{}] requested a manual synchronization of all platforms", email);

        rateLimiterService.checkRateLimit(email, "sync_all_platforms");

        SyncResponse response = syncService.syncAllPlatformResponse(email);

        return ResponseEntity.ok(response);
    }

}
