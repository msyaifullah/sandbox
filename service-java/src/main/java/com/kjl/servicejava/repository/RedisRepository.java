package com.kjl.servicejava.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

/**
 * Repository pattern for Redis operations.
 * Encapsulates all Redis data access logic.
 */
@Repository
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }

    public void setSearchResult(String queryId, String source, int index, String resultJson) {
        String key = String.format("search_result:%s:%s:%d", queryId, source, index);
        redisTemplate.opsForValue().set(key, resultJson, Duration.ofMinutes(30));
    }

    public String getProgress(String queryId) {
        return get("progress:" + queryId);
    }

    public void setProgress(String queryId, int receivedFlights) {
        set("progress:" + queryId, String.valueOf(receivedFlights), Duration.ofMinutes(30));
    }

    // Redis list operations for long polling
    public void pushFlightResult(String queryId, String resultJson) {
        String key = "flight_results:" + queryId;
        redisTemplate.opsForList().rightPush(key, resultJson);
        redisTemplate.expire(key, Duration.ofMinutes(30));
    }

    public List<String> getFlightResults(String queryId, long start, long end) {
        String key = "flight_results:" + queryId;
        return redisTemplate.opsForList().range(key, start, end);
    }

    public void incrementFlightCount(String queryId) {
        String key = "flight_count:" + queryId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(30));
    }

    public int getFlightCount(String queryId) {
        String key = "flight_count:" + queryId;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return 0;
        }
        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void deleteFlightResults(String queryId) {
        redisTemplate.delete("flight_results:" + queryId);
        redisTemplate.delete("flight_count:" + queryId);
    }
}
