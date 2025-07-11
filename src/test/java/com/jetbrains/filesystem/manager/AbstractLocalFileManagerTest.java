package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.service.LocalFileManager;
import com.jetbrains.filesystem.storage.FileStorage;
import com.jetbrains.filesystem.util.PathValidator;
import com.jetbrains.filesystem.lock.FileLockRegistry;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.*;

public abstract class AbstractLocalFileManagerTest {
    protected FileStorage storage;
    protected PathValidator validator;
    protected FileLockRegistry lockRegistry;
    protected LocalFileManager manager;

    @BeforeEach
    void initManager() {
        this.storage = mock(FileStorage.class);
        this.validator = mock(PathValidator.class);
        this.lockRegistry = mock(FileLockRegistry.class);
        this.manager = new LocalFileManager(storage, validator);
    }

}

