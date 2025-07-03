package com.jetbrains.filesystem.service;

import com.jetbrains.filesystem.config.FileServiceProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileServiceReadFileSegmentExceptionTest {
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
    void testReadFileSegment_PathOutsideRoot_ShouldThrowException() {
        String path = "../../etc/passwd";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.readFile(path, 0, 10);
        });

        assertTrue(ex.getMessage().contains("Outside root folder"));
    }

    @Test
    void testReadFileSegment_FileDoesNotExist_ShouldThrowFileNotFoundException() {
        String path = "nonexistent.txt";

        FileNotFoundException ex = assertThrows(FileNotFoundException.class, () -> {
            fileService.readFile(path, 0, 10);
        });

        assertTrue(ex.getMessage().contains("File not found"));
    }

    @Test
    void testReadFileSegment_PathIsDirectory_ShouldThrowException() throws IOException {
        String path = "test-folder/dir";
        Files.createDirectories(root.resolve(path));

        FileNotFoundException ex = assertThrows(FileNotFoundException.class, () -> {
            fileService.readFile(path, 0, 10);
        });

        assertTrue(ex.getMessage().contains("File not found"));
    }

    @Test
    void testReadFileSegment_NegativeOffset_ShouldThrowException() {
        String path = "test-folder/file.txt";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.readFile(path, -5, 10);
        });

        assertTrue(ex.getMessage().contains("offset must be in range"));
    }

    @Test
    void testReadFileSegment_ZeroOrNegativeLength_ShouldThrowException() {
        String path = "test-folder/file.txt";

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            fileService.readFile(path, 0, 0);
        });

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            fileService.readFile(path, 0, -1);
        });

        assertTrue(ex1.getMessage().contains("length must be positive"));
        assertTrue(ex2.getMessage().contains("length must be positive"));
    }

    @Test
    void testReadFileSegment_OffsetExceedsFileLength_ShouldThrowException() throws IOException {
        String path = "test-folder/sample.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "abcde"); // length = 5

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.readFile(path, 100, 10);
        });

        assertTrue(ex.getMessage().contains("offset must be in range"));
    }

}

