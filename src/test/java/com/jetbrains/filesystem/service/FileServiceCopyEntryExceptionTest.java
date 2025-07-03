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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileServiceCopyEntryExceptionTest {

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
    void testCopyEntry_SourcePathOutsideRoot_ShouldThrowException() {
        String source = "../../outside.txt";
        String target = "safe-folder/target.txt";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.copyEntry(source, target);
        });

        assertTrue(ex.getMessage().contains("Outside root folder"));
    }

    @Test
    void testCopyEntry_TargetPathOutsideRoot_ShouldThrowException() throws IOException {
        String source = "test-folder/source.txt";
        Files.createDirectories(root.resolve("test-folder"));
        Files.writeString(root.resolve(source), "test");

        String target = "../../etc/passwd";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.copyEntry(source, target);
        });

        assertTrue(ex.getMessage().contains("Outside root folder"));
    }

    @Test
    void testCopyEntry_SourceDoesNotExist_ShouldThrowFileNotFoundException() {
        String source = "nonexistent-file.txt";
        String target = "test-folder/target.txt";

        FileNotFoundException ex = assertThrows(FileNotFoundException.class, () -> {
            fileService.copyEntry(source, target);
        });

        System.out.println(ex.toString());
        assertTrue(ex.getMessage().contains("source not found"));
    }

    @Test
    void testCopyEntry_TargetAlreadyExists_ShouldThrowIllegalArgumentException() throws IOException {
        String source = "test-folder/copy-source.txt";
        String target = "test-folder/copy-target.txt";

        Path sourcePath = root.resolve(source);
        Path targetPath = root.resolve(target);

        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "data");
        Files.writeString(targetPath, "exists");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            fileService.copyEntry(source, target);
        });

        assertTrue(ex.getMessage().contains("target already exists"));
    }

    @Test
    void testCopyEntry_CopyFailsDueToPermission_ShouldThrowIOException() throws IOException {
        FileManageService fileService = spy(new FileManageService(properties));
        String source = "test-folder/copy-permission.txt";
        String target = "test-folder/restricted-dir/copied.txt";

        Path sourcePath = root.resolve(source);
        Path restrictedDir = root.resolve("test-folder/restricted-dir");

        Files.createDirectories(restrictedDir);
        Files.writeString(sourcePath, "data");

        // mock deletePath throw exception
        doThrow(new IOException("Simulated copy failure"))
                .when(fileService).copyPath(any(Path.class),any(Path.class));

        IOException ex = assertThrows(IOException.class, () -> {
            fileService.copyEntry(source, target);
        });

        assertTrue( ex.getMessage().contains("Failed to copy file"));
    }

}

