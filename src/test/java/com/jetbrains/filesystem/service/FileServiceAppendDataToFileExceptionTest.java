package com.jetbrains.filesystem.service;

import com.jetbrains.filesystem.config.FileServiceProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileServiceAppendDataToFileExceptionTest {

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
    void testAppendDataToFile_PathOutsideRoot_ShouldThrowException() {
        String path = "../../outside.txt";
        String data = Base64.getEncoder().encodeToString("test".getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.appendDataToFile(path, data);
        });

        assertTrue(ex.getMessage().contains("Outside root folder"));
    }

    @Test
    void testAppendDataToFile_FileDoesNotExist_ShouldThrowFileNotFoundException() {
        String path = "nonexistent.txt";
        String data = Base64.getEncoder().encodeToString("test".getBytes(StandardCharsets.UTF_8));

        FileNotFoundException ex = assertThrows(FileNotFoundException.class, () -> {
            fileService.appendDataToFile(path, data);
        });

        assertTrue(ex.getMessage().contains("File not found"));
    }

    @Test
    void testAppendDataToFile_PathIsDirectory_ShouldThrowException() throws IOException {
        String path = "test-folder/dir";
        Path dirPath = root.resolve(path);
        Files.createDirectories(dirPath);

        String data = Base64.getEncoder().encodeToString("data".getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.appendDataToFile(path, data);
        });

        assertTrue(ex.getMessage().contains("Can't append to a directory"));
    }

    @Test
    void testAppendDataToFile_InvalidBase64_ShouldThrowException() throws IOException {
        String path = "test-folder/valid.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "");

        String invalidBase64 = "@@@!!==invalid";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.appendDataToFile(path, invalidBase64);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("failed to append file content"));
    }

    @Test
    void testAppendDataToFile_WriteFails_ShouldThrowIOException() throws IOException {
        FileManageService fileService = spy(new FileManageService(properties));
        String path = "test-folder/protected.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "existing");
        String data = Base64.getEncoder().encodeToString("data".getBytes(StandardCharsets.UTF_8));

        // mock deletePath throw exception
        doThrow(new IOException("no permission to write"))
                .when(fileService).appendFileContent(any(Path.class),any(File.class), any(String.class));

        IOException ex = assertThrows(IOException.class, () -> {
            fileService.appendDataToFile(path, data);
        });

        System.out.println(ex.toString());
        assertTrue(ex.getMessage().toLowerCase().contains("no permission to write"));
    }

}

