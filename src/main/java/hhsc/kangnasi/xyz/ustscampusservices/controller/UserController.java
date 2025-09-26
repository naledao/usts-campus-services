package hhsc.kangnasi.xyz.ustscampusservices.controller;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RequestMapping("/user")
@RestController
public class UserController {

    private final RedissonClient redissonClient;

    public UserController(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @GetMapping("/ping-redis")
    public String pingRedis() {
        RBucket<String> bucket = redissonClient.getBucket("usts:ping");
        String value = "pong@" + Instant.now();
        bucket.set(value);
        return bucket.get();
    }

    @GetMapping("/lock-increment")
    public long lockIncrement() {
        var lock = redissonClient.getLock("usts:lock");
        boolean acquired = false;
        try {
            acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("Could not acquire lock");
            }
            RAtomicLong counter = redissonClient.getAtomicLong("usts:counter");
            return counter.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
