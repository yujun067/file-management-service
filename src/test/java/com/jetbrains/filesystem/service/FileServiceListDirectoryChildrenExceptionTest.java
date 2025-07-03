package com.jetbrains.filesystem.service;

import com.jetbrains.filesystem.config.FileServiceProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileServiceListDirectoryChildrenExceptionTest {

    @Autowired
    private FileManageService fileService;

    @Autowired
    private FileServiceProperties properties;

    private Path root;

    @BeforeEach
    void setup() {
        root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
    }

    @AfterEach
    void tearDown() throws IOException {
        Path testDir = root.resolve("test-folder");
        if (Files.exists(testDir)) {
            deleteRecursively(testDir);
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        if (Files.isDirectory(path)) {
            try (Stream<Path> paths = Files.list(path)) {
                for (Path child : paths.toList()) {
                    deleteRecursively(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    @Test
    void testListDirectoryChildren_PathOutsideRoot_ShouldThrowException() {
        String maliciousPath = "../../etc";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.listDirectoryChildren(maliciousPath);
        });

        assertTrue(ex.getMessage().contains("Outside root folder"));
    }

    @Test
    void testListDirectoryChildren_FileNotDirectory_ShouldThrowException() throws IOException {
        String filePath = "test-folder/not-a-dir.txt";
        Path fullPath = root.resolve(filePath);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, "this is a file");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.listDirectoryChildren(filePath);
        });

        assertTrue(ex.getMessage().contains("not a directory"));
    }

    @Test
    void testListDirectoryChildren_NonExistentPath_ShouldThrowException() {
        String nonExisting = "nonexistent-folder";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.listDirectoryChildren(nonExisting);
        });

        assertTrue(ex.getMessage().contains("not a directory"));
    }

    @Test
    void testListDirectoryChildren_NullOrEmptyPath_ShouldThrowIllegalArgumentException() {
        // Test null
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            fileService.listDirectoryChildren(null);
        });

        // Test empty string
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            fileService.listDirectoryChildren("");
        });

        assertTrue(exception1.getMessage().contains("Invalid path"));
        assertTrue(exception2.getMessage().contains("Invalid path"));
    }
}
