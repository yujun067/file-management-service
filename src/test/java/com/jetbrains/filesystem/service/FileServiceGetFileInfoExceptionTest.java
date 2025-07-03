package com.jetbrains.filesystem.service;

import com.jetbrains.filesystem.config.FileServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileServiceGetFileInfoExceptionTest {

    @Autowired
    private FileManageService fileService;

    @Autowired
    private FileServiceProperties properties;

    private Path root;

    @BeforeEach
    void setUp() {
        root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
    }

    @Test
    void testGetFileInfo_InvalidPath_OutOfRoot_ShouldThrowException() {
        String maliciousPath = "../outside-file.txt";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileService.getFileInfo(maliciousPath);
        });

        assertTrue(exception.getMessage().contains("Outside root folder"));
    }

    @Test
    void testGetFileInfo_FileNotExist_ShouldThrowFileNotFoundException() {
        String nonExistingFile = "non-existent-file.txt";

        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            fileService.getFileInfo(nonExistingFile);
        });

        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testGetFileInfo_NullOrEmptyPath_ShouldThrowIllegalArgumentException() {
        // Test null
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            fileService.getFileInfo(null);
        });

        // Test empty string
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            fileService.getFileInfo("");
        });

        assertTrue(exception1.getMessage().contains("Invalid path"));
        assertTrue(exception2.getMessage().contains("Invalid path"));
    }
}

