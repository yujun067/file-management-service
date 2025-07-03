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
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileServiceDeleteEntryExceptionTest {

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
    void testDeleteEntry_PathOutsideRoot_ShouldThrowException() {
        String path = "../../outside.txt";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.deleteEntry(path);
        });

        assertTrue(ex.getMessage().contains("Outside root folder"));
    }

    @Test
    void testDeleteEntry_NonExistentFile_ShouldThrowFileNotFoundException() {
        String path = "non-existent-file.txt";

        FileNotFoundException ex = assertThrows(FileNotFoundException.class, () -> {
            fileService.deleteEntry(path);
        });

        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testDeleteEntry_DeletionFails_ShouldThrowIOException() throws IOException {
        FileManageService fileService = spy(new FileManageService(properties));
        String path = "test-folder/protected.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "can't delete this");

        // mock deletePath throw exception
        doThrow(new IOException("Simulated deletion failure"))
                .when(fileService).deletePath(any(Path.class));

        IOException ex = assertThrows(IOException.class, () -> {
            fileService.deleteEntry(path);
        });

        assertTrue(ex.getMessage().contains("Failed to delete"));
    }

}

