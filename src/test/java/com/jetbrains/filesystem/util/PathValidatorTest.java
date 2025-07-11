package com.jetbrains.filesystem.util;

import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.exception.ConflictException;
import com.jetbrains.filesystem.exception.NotFoundException;
import com.jetbrains.filesystem.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class PathValidatorTest {

    @TempDir
    Path tempRoot;

    private PathValidator validator;

    @BeforeEach
    void setUp() {
        FileServiceProperties props = new FileServiceProperties();
        props.setRootFolder(tempRoot.toString());
        validator = new PathValidator(props);
    }

    @Test
    void toAbsolute_shouldResolveValidRelativePath() {
        Path result = validator.toAbsolute("a/b.txt");
        assertTrue(result.toString().startsWith(tempRoot.toString()));
    }

    @Test
    void toAbsolute_shouldRejectBlankPath() {
        assertThrows(ValidationException.class, () -> validator.toAbsolute(" "));
    }

    @Test
    void toAbsolute_shouldRejectOutsideRoot() {
        assertThrows(ValidationException.class, () -> validator.toAbsolute("../../etc/passwd"));
    }

    @Test
    void toRelative_shouldReturnCleanRelativePath() {
        Path file = tempRoot.resolve("sub/file.txt");
        String result = validator.toRelative(file.toString());
        assertEquals("sub/file.txt", result);
    }

    @Test
    void toRelative_shouldRejectPathOutsideRoot() {
        assertThrows(ValidationException.class, () -> validator.toRelative("/etc/passwd"));
    }

    @Test
    void validatePath_shouldPassForValidSourceAndTarget() throws IOException {
        Path source = Files.createFile(tempRoot.resolve("source.txt"));
        Path target = tempRoot.resolve("target.txt");
        assertDoesNotThrow(() -> validator.validatePath(source, target));
    }

    @Test
    void validatePath_shouldRejectIfSourceNotExist() {
        Path source = tempRoot.resolve("missing.txt");
        Path target = tempRoot.resolve("target.txt");

        assertThrows(NotFoundException.class, () -> validator.validatePath(source, target));
    }

    @Test
    void validatePath_shouldRejectIfTargetAlreadyExists() throws IOException {
        Path source = Files.createFile(tempRoot.resolve("source.txt"));
        Path target = Files.createFile(tempRoot.resolve("target.txt"));

        assertThrows(ConflictException.class, () -> validator.validatePath(source, target));
    }

    @Test
    void validatePath_shouldRejectIfTargetInsideSource() throws IOException {
        Path source = tempRoot.resolve("folder");
        Path target = source.resolve("subfolder");

        Files.createDirectories(source);
        Files.createDirectories(target);

        assertThrows(ConflictException.class, () -> validator.validatePath(source, target));
    }

    @Test
    void validateSourceForAppend_shouldPassForValidFile() throws IOException {
        Path file = Files.createFile(tempRoot.resolve("appendable.txt"));
        assertDoesNotThrow(() -> validator.validateSourceForAppend(file));
    }

    @Test
    void validateSourceForAppend_shouldRejectIfNotExists() {
        Path file = tempRoot.resolve("ghost.txt");
        assertThrows(NotFoundException.class, () -> validator.validateSourceForAppend(file));
    }

    @Test
    void validateSourceForAppend_shouldRejectIfDirectory() throws IOException {
        Path dir = Files.createDirectory(tempRoot.resolve("dir"));
        assertThrows(ValidationException.class, () -> validator.validateSourceForAppend(dir));
    }
}
