package com.jetbrains.filesystem.lock;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class LocalCaffeineLockRegistry implements FileLockRegistry {
    private final Cache<String, ReentrantLock> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @Override
    public ReentrantLock lock(String key) { return cache.get(key, k -> new ReentrantLock()); }
}

