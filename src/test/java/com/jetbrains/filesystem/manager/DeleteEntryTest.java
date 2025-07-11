package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.dto.file.DeleteEntryResponse;
import com.jetbrains.filesystem.exception.NotFoundException;
import com.jetbrains.filesystem.exception.ValidationException;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeleteEntryTest extends AbstractLocalFileManagerTest {

    @Test
    void shouldDeleteFileSuccessfully() {
        String relPath = "old-file.txt";
        Path absPath = Path.of("/root/old-file.txt");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);

        DeleteEntryResponse response = manager.deleteEntry(relPath);

        verify(storage).delete(absPath);
        assertEquals(relPath, response.getPath());
    }

    @Test
    void shouldThrowNotFoundIfFileDoesNotExist() {
        String relPath = "missing.txt";
        Path absPath = Path.of("/root/missing.txt");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        doThrow(new NotFoundException("File not found")).when(storage).delete(absPath);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> manager.deleteEntry(relPath));
        assertEquals("File not found", ex.getMessage());
    }

    @Test
    void shouldThrowValidationIfPathInvalid() {
        String relPath = "../etc/passwd";

        when(validator.toAbsolute(relPath)).thenThrow(new ValidationException("outside root"));

        assertThrows(ValidationException.class, () -> manager.deleteEntry(relPath));
        verify(storage, never()).copy(any(), any());
    }
}
