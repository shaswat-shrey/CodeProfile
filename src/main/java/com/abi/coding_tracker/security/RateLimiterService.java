package com.abi.coding_tracker.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.abi.coding_tracker.exception.ExternalApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RateLimiterService {
    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUEST = 10;
    private static final Duration TIME_WINDOW = Duration.ofSeconds(60);

    public RateLimiterService(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public void checkRateLimit(String userEmail, String apiName){
        // Creates a unique key like: rate_limit:leetcode:choudhary.abhishek332@gmail.com
        String key = "rate_limit:"+apiName+":"+userEmail;

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if(currentCount != null){
            if(currentCount == 1){
                redisTemplate.expire(key, TIME_WINDOW);
            }else if(currentCount > MAX_REQUEST){
                log.warn("Rate limit exeeded for user {} on API {}", userEmail, apiName);
                throw new ExternalApiException("Too many request wait a minute before tring again", 429);
            }
        }
    }
}
