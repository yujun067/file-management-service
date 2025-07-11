package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.dto.file.MoveEntryResponse;
import com.jetbrains.filesystem.exception.ConflictException;
import com.jetbrains.filesystem.exception.NotFoundException;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MoveEntryTest extends AbstractLocalFileManagerTest {

    @Test
    void shouldMoveEntrySuccessfully() {
        String sourceRel = "docs/old.txt";
        String targetRel = "docs/new.txt";

        Path sourceAbs = Path.of("/root/docs/old.txt");
        Path targetAbs = Path.of("/root/docs/new.txt");

        when(validator.toAbsolute(sourceRel)).thenReturn(sourceAbs);
        when(validator.toAbsolute(targetRel)).thenReturn(targetAbs);

        MoveEntryResponse response = manager.moveEntry(sourceRel, targetRel);

        verify(storage).move(sourceAbs, targetAbs);
        assertEquals(sourceRel, response.getSourcePath());
        assertEquals(targetRel, response.getTargetPath());
    }

    @Test
    void shouldThrowNotFoundIfSourceNotExist() {
        String sourceRel = "missing.txt";
        String targetRel = "dest.txt";

        Path sourceAbs = Path.of("/root/missing.txt");
        Path targetAbs = Path.of("/root/dest.txt");

        when(validator.toAbsolute(targetRel)).thenReturn(targetAbs);
        when(validator.toAbsolute(sourceRel)).thenThrow(new NotFoundException("File not found"));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> manager.moveEntry(sourceRel, targetRel));
        assertEquals("File not found", ex.getMessage());
        verify(storage, never()).copy(any(), any());
    }

    @Test
    void shouldThrowConflictIfTargetAlreadyExists() {
        String sourceRel = "a.txt";
        String targetRel = "b.txt";

        Path sourceAbs = Path.of("/root/a.txt");
        Path targetAbs = Path.of("/root/b.txt");

        when(validator.toAbsolute(sourceRel)).thenReturn(sourceAbs);
        when(validator.toAbsolute(sourceRel)).thenThrow(new ConflictException("target already exists"));

        assertThrows(ConflictException.class, () -> manager.moveEntry(sourceRel, targetRel));
        verify(storage, never()).copy(any(), any());
    }

    @Test
    void shouldThrowValidationIfTargetInsideSource() {
        String sourceRel = "folder";
        String targetRel = "folder/subfolder";

        Path sourceAbs = Path.of("/root/folder");
        Path targetAbs = Path.of("/root/folder/subfolder");

        when(validator.toAbsolute(sourceRel)).thenReturn(sourceAbs);
        when(validator.toAbsolute(sourceRel)).thenThrow(new ConflictException("own subdirectories"));

        assertThrows(ConflictException.class, () -> manager.moveEntry(sourceRel, targetRel));
        verify(storage, never()).copy(any(), any());
    }
}
