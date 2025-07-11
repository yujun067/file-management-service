package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.dto.file.ReadFileSegmentResponse;
import com.jetbrains.filesystem.exception.NotFoundException;
import com.jetbrains.filesystem.exception.ValidationException;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReadFileSegmentTest extends AbstractLocalFileManagerTest {

    @Test
    void shouldReadFileSegmentSuccessfully() {
        String relPath = "data.txt";
        Path absPath = Path.of("/root/data.txt");
        long offset = 0;
        int length = 5;
        byte[] data = "Hello".getBytes();
        String expectedBase64 = Base64.getEncoder().encodeToString(data);

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        when(storage.read(absPath, offset, length)).thenReturn(ByteBuffer.wrap(data));

        ReadFileSegmentResponse response = manager.readFile(relPath, offset, length);

        assertEquals(expectedBase64, response.getData());
    }

    @Test
    void shouldThrowValidationExceptionIfLengthNonPositive() {
        String relPath = "log.txt";
        Path absPath = Path.of("/root/log.txt");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        doThrow(new ValidationException("length must be positive")).when(storage).read(absPath, 0, 0);

        assertThrows(ValidationException.class, () ->
                manager.readFile(relPath, 0, 0));

    }

    @Test
    void shouldThrowNotFoundIfFileDoesNotExist() {
        String relPath = "missing.txt";
        Path absPath = Path.of("/root/missing.txt");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        doThrow(new NotFoundException("File not found")).when(storage).read(absPath, 0, 5);

        assertThrows(NotFoundException.class, () -> manager.readFile(relPath, 0, 5));
    }

    @Test
    void shouldThrowValidationIfPathOutsideRoot() {
        String relPath = "../outside";

        when(validator.toAbsolute(relPath)).thenThrow(new ValidationException("outside root"));

        assertThrows(ValidationException.class, () -> manager.readFile(relPath, 0, 5));
        verify(storage, never()).copy(any(), any());
    }

}
