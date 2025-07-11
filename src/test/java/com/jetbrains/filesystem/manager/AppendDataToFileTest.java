package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.dto.file.AppendDataToFileResponse;
import com.jetbrains.filesystem.exception.NotFoundException;
import com.jetbrains.filesystem.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AppendDataToFileTest extends AbstractLocalFileManagerTest {

    private final ReentrantLock testLock = new ReentrantLock();

    @BeforeEach
    void setupLock() {
        when(lockRegistry.lock(anyString())).thenReturn(testLock);
    }

    @Test
    void shouldAppendDataSuccessfully() {
        String relPath = "log.txt";
        Path absPath = Path.of("/root/log.txt");
        String original = "Hello world";
        String base64 = Base64.getEncoder().encodeToString(original.getBytes());

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        when(storage.append(absPath, original.getBytes())).thenReturn(original.length());

        AppendDataToFileResponse response = manager.appendDataToFile(relPath, base64);

        assertEquals(relPath, response.getPath());
        assertEquals(original.length(), response.getAppendLength());
    }

    @Test
    void shouldThrowValidationExceptionIfBase64Invalid() {
        String relPath = "bad.txt";
        Path absPath = Path.of("/root/bad.txt");
        String invalidBase64 = "###invalid###";

        when(validator.toAbsolute(relPath)).thenReturn(absPath);

        assertThrows(ValidationException.class, () ->
                manager.appendDataToFile(relPath, invalidBase64));
        verify(storage, never()).copy(any(), any());
    }

    @Test
    void shouldThrowNotFoundIfFileMissing() {
        String relPath = "ghost.txt";
        Path absPath = Path.of("/root/ghost.txt");
        String base64 = Base64.getEncoder().encodeToString("hi".getBytes());

        when(validator.toAbsolute(relPath)).thenThrow(new NotFoundException("File not found"));

        assertThrows(NotFoundException.class, () -> manager.appendDataToFile(relPath, base64));
        verify(storage, never()).copy(any(), any());
    }

    @Test
    void shouldThrowValidationIfPathOutsideRoot() {
        String relPath = "../etc/passwd";
        when(validator.toAbsolute(relPath)).thenThrow(new ValidationException("outside root"));

        assertThrows(ValidationException.class, () ->
                manager.appendDataToFile(relPath, "SGVsbG8="));
        verify(storage, never()).copy(any(), any());
    }
}
