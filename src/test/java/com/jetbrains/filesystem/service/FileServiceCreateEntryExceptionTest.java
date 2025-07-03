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
public class FileServiceCreateEntryExceptionTest {

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
    void testCreateEntry_InvalidType_ShouldThrowIllegalArgumentException() {
        String path = "test-folder/invalid.txt";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.createEntry(path, "badtype");
        });

        assertTrue(ex.getMessage().contains("type must be 'file' or 'folder'"));
    }

    @Test
    void testCreateEntry_PathOutsideRoot_ShouldThrowException() {
        String path = "../../etc/passwd";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.createEntry(path, "file");
        });

        assertTrue(ex.getMessage().contains("Outside root folder"));
    }

    @Test
    void testCreateEntry_PathAlreadyExists_ShouldThrowException() throws IOException {
        String path = "test-folder/existing.txt";
        Path fullPath = root.resolve(path);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, "exists");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.createEntry(path, "file");
        });

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testCreateEntry_CreationFails_ShouldThrowIOException() throws IOException {
        // mock unwritable directory
        String path = "test-folder/no-permission/file.txt";
        Path parentDir = root.resolve("test-folder/no-permission");
        Files.createDirectories(parentDir);
        parentDir.toFile().setWritable(false);

        IOException ex = assertThrows(IOException.class, () -> {
            fileService.createEntry("test-folder/no-permission/file.txt", "file");
        });

        // recover permission
        parentDir.toFile().setWritable(true);

        assertTrue(ex.getMessage().contains("Failed to create"));
    }
}

