package com.jetbrains.filesystem.lock;

import java.util.concurrent.locks.ReentrantLock;

public interface FileLockRegistry {
    ReentrantLock lock(String key);
}
