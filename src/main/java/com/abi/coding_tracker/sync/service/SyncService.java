package com.abi.coding_tracker.sync.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.platform.CodingPlatformService;
import com.abi.coding_tracker.repository.UserRepository;
import com.abi.coding_tracker.sync.dto.SyncResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SyncService {
    private final UserRepository userRepository;
    private final List<CodingPlatformService> platformServices;

    public SyncService(UserRepository userRepository, List<CodingPlatformService> platformServices){
        this.userRepository = userRepository;
        this.platformServices = platformServices;
    }

    public SyncResponse syncAllPlatformResponse(String email){
        log.info("Starting synchronization orchestration for user: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User Not found"));

        return excecuteSyncForUser(user);
    }

    public void syncAllUsers(){
        List<User> users = userRepository.findAll();
        log.info("Found {} users in the database. Beginning batch synchronization.", users.size());

        int successfulUsers = 0;
        int failedUsers = 0;

        for(User user : users){
            try {
                SyncResponse response = excecuteSyncForUser(user);

                if("FAILED".equals(response.getStatus())){
                    failedUsers++;
                }else{
                    successfulUsers++;
                }
            } catch (Exception e) {
                log.error("Catastrophic failure during synchronization for user ID {}: {}", user.getId(), e.getMessage());
                failedUsers++;
            }
        }

        log.info("Batch synchronization complete. Success: {}, Failed: {}", successfulUsers, failedUsers);
    }


    private SyncResponse excecuteSyncForUser(User user){
        LocalDateTime startedAt = LocalDateTime.now();

        Map<String, String> platformStatuses = new HashMap<>();
        int successCount =0;
        int failCount = 0;
        int connectedCount = 0;

        for(CodingPlatformService service : platformServices){
            String platformName = service.getPlatformName();

            try{
                if(service.getLastSync(user) != null){
                    connectedCount++;
                    log.info("Synchronizing {}...", platformName);

                    service.refresh(user);

                    platformStatuses.put(platformName, "SUCCESS");
                    successCount++;
                }else{
                    platformStatuses.put(platformName, "NOT_CONNECTED");
                }
            }catch(Exception e){
                log.error("Failed to sync {}: {}", platformName, e.getMessage());
                platformStatuses.put(platformName, "FAILED");
                failCount++;
            }
        }

        String overallStatus;
        if(connectedCount == 0) overallStatus = "NO_ACCOUNTS_CONNECTED";
        else if(failCount==0) overallStatus = "COMPLETED";
        else if(successCount > 0) overallStatus = "PARTIAL_SUCCESS";
        else overallStatus ="FAILED";
        
        return SyncResponse.builder()
                .status(overallStatus)
                .startedAt(startedAt)
                .completedAt(LocalDateTime.now())
                .platforms(platformStatuses)
                .build();
    }

}
