package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.dto.file.CreateEntryResponse;
import com.jetbrains.filesystem.dto.file.EntryType;
import com.jetbrains.filesystem.exception.ConflictException;
import com.jetbrains.filesystem.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class CreateEntryTest extends AbstractLocalFileManagerTest {

    @Test
    void shouldCreateFileSuccessfully() {
        String relPath = "new-file.txt";
        Path absPath = Path.of("/root/new-file.txt");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);

        CreateEntryResponse response = manager.createEntry(relPath, "file");

        verify(storage).create(absPath, EntryType.FILE);
        assertEquals("new-file.txt", response.getName());
        assertEquals(relPath, response.getPath());
    }

    @Test
    void shouldCreateFolderSuccessfully() {
        String relPath = "new-folder";
        Path absPath = Path.of("/root/new-folder");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);

        CreateEntryResponse response = manager.createEntry(relPath, "folder");

        verify(storage).create(absPath, EntryType.FOLDER);
        assertEquals("new-folder", response.getName());
        assertEquals(relPath, response.getPath());
    }

    @Test
    void shouldThrowValidationExceptionForInvalidType() {
        String relPath = "bad.txt";

        assertThrows(ValidationException.class, () -> manager.createEntry(relPath, "image"));
    }

    @Test
    void shouldThrowConflictIfAlreadyExists() {
        String relPath = "exists.txt";
        Path absPath = Path.of("/root/exists.txt");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        doThrow(new ConflictException("already exists")).when(storage).create(absPath, EntryType.FILE);

        assertThrows(ConflictException.class, () -> manager.createEntry(relPath, "file"));
    }

    @Test
    void shouldThrowValidationIfPathIsInvalid() {
        String relPath = "";
        when(validator.toAbsolute(relPath)).thenThrow(new ValidationException("path empty"));

        assertThrows(ValidationException.class, () -> manager.createEntry(relPath, "file"));
        verify(storage, never()).copy(any(), any());
    }
}
