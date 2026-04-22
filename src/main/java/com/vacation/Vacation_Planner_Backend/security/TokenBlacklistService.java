package com.vacation.Vacation_Planner_Backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // Add token to blacklist with TTL equal to token's remaining lifetime
    public void blacklistToken(String token, long expirationMs) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", expirationMs, TimeUnit.MILLISECONDS);
    }

    // Check if token is in blacklist
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}