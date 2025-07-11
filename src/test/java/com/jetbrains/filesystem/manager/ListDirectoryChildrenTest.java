package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.dto.file.FileInfo;
import com.jetbrains.filesystem.dto.file.ListDirectoryResponse;
import com.jetbrains.filesystem.exception.NotFoundException;
import com.jetbrains.filesystem.exception.ValidationException;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ListDirectoryChildrenTest extends AbstractLocalFileManagerTest {

    @Test
    void shouldListChildrenSuccessfully() {
        String relPath = "folder";
        Path absPath = Path.of("/root/folder");

        List<FileInfo> mockChildren = List.of(
                new FileInfo("a.txt", "/root/folder/a.txt",100L, false),
                new FileInfo("subdir", "/root/folder/subdir",0L, true)
        );

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        when(validator.toRelative("/root/folder/a.txt")).thenReturn("folder/a.txt");
        when(validator.toRelative("/root/folder/subdir")).thenReturn("folder/subdir");

        when(storage.getFileInfolist(absPath)).thenReturn(mockChildren);

        ListDirectoryResponse response = manager.listDirectoryChildren(relPath);

        assertEquals(2, response.getFileInfos().size());

        FileInfo first = response.getFileInfos().get(0);
        assertEquals("a.txt", first.getName());
        assertEquals("folder/a.txt", first.getPath());
        assertFalse(first.isDirectory());

        FileInfo second = response.getFileInfos().get(1);
        assertEquals("subdir", second.getName());
        assertEquals("folder/subdir", second.getPath());
        assertTrue(second.isDirectory());
    }

    @Test
    void shouldThrowWhenDirectoryNotExist() {
        String relPath = "ghost";
        Path absPath = Path.of("/root/ghost");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        when(storage.getFileInfolist(absPath)).thenThrow(new NotFoundException("directory not found"));

        assertThrows(NotFoundException.class, () -> manager.listDirectoryChildren(relPath));
    }

    @Test
    void shouldThrowIfPathInvalid() {
        String relPath = "../outside";

        when(validator.toAbsolute(relPath)).thenThrow(new ValidationException("outside root"));

        assertThrows(ValidationException.class, () -> manager.listDirectoryChildren(relPath));
        verify(storage, never()).copy(any(), any());
    }
}

