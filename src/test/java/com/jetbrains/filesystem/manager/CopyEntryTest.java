package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.dto.file.CopyEntryResponse;
import com.jetbrains.filesystem.exception.ConflictException;
import com.jetbrains.filesystem.exception.NotFoundException;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CopyEntryTest extends AbstractLocalFileManagerTest {

    @Test
    void shouldCopyEntrySuccessfully() {
        String sourceRel = "source.txt";
        String targetRel = "copied.txt";

        Path sourceAbs = Path.of("/root/source.txt");
        Path targetAbs = Path.of("/root/copied.txt");

        when(validator.toAbsolute(sourceRel)).thenReturn(sourceAbs);
        when(validator.toAbsolute(targetRel)).thenReturn(targetAbs);

        CopyEntryResponse response = manager.copyEntry(sourceRel, targetRel);
        verify(storage).copy(sourceAbs, targetAbs);
        assertEquals(sourceRel, response.getSourcePath());
        assertEquals(targetRel, response.getTargetPath());
    }

    @Test
    void shouldThrowNotFoundIfSourceMissing() {
        String sourceRel = "ghost.txt";
        String targetRel = "new.txt";

        Path sourceAbs = Path.of("/root/ghost.txt");
        Path targetAbs = Path.of("/root/new.txt");

        when(validator.toAbsolute(sourceRel)).thenReturn(sourceAbs);
        when(validator.toAbsolute(targetRel)).thenThrow(new NotFoundException("File not found"));

        assertThrows(NotFoundException.class, () -> manager.copyEntry(sourceRel, targetRel));
        verify(storage, never()).copy(any(), any());
    }

    @Test
    void shouldThrowConflictIfTargetAlreadyExists() {
        String sourceRel = "a.txt";
        String targetRel = "a_copy.txt";

        Path sourceAbs = Path.of("/root/a.txt");
        Path targetAbs = Path.of("/root/a_copy.txt");

        when(validator.toAbsolute(sourceRel)).thenReturn(sourceAbs);
        when(validator.toAbsolute(targetRel)).thenReturn(targetAbs);

        doThrow(new ConflictException("target already exists"))
                .when(validator).validatePath(sourceAbs, targetAbs);

        assertThrows(ConflictException.class, () -> manager.copyEntry(sourceRel, targetRel));
        verify(storage, never()).copy(any(), any());
    }

    @Test
    void shouldThrowValidationIfTargetIsSubdirOfSource() {
        String sourceRel = "folder";
        String targetRel = "folder/subfolder";

        Path sourceAbs = Path.of("/root/folder");
        Path targetAbs = Path.of("/root/folder/subfolder");

        when(validator.toAbsolute(sourceRel)).thenReturn(sourceAbs);
        when(validator.toAbsolute(targetRel)).thenReturn(targetAbs);

        doThrow(new ConflictException("own subdirectories"))
                .when(validator).validatePath(sourceAbs, targetAbs);

        assertThrows(ConflictException.class, () -> manager.copyEntry(sourceRel, targetRel));
        verify(storage, never()).copy(any(), any());
    }
}
