package com.abi.coding_tracker.sync.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Map<String,String> platforms;
}
