package br.com.washington.cloudflare_service.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RedisTestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostConstruct
    public void testRedisConnection() {
        try {
            String pingResult = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().ping();
            System.out.println("Redis Connection Successful: " + pingResult);
        } catch (Exception e) {
            System.err.println("Redis Connection Failed: " + e.getMessage());
        }
    }
}
