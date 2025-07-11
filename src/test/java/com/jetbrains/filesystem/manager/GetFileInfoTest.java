package com.jetbrains.filesystem.manager;

import com.jetbrains.filesystem.dto.file.FileInfo;
import com.jetbrains.filesystem.dto.file.GetFileInfoResponse;
import com.jetbrains.filesystem.exception.NotFoundException;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetFileInfoTest extends AbstractLocalFileManagerTest {

    @Test
    void shouldReturnFileInfoSuccessfully() {
        String relativePath = "docs/file.txt";
        Path absPath = Path.of("/root/docs/file.txt");

        when(validator.toAbsolute(relativePath)).thenReturn(absPath);
        when(validator.toRelative(absPath.toString())).thenReturn(relativePath);
        when(storage.getFileInfo(absPath)).thenReturn(new FileInfo("file.txt", "/root/docs/file.txt",1024L, false));

        GetFileInfoResponse response = manager.getFileInfo(relativePath);

        assertEquals("file.txt", response.getName());
        assertEquals(relativePath, response.getPath());
        assertEquals(1024L, response.getSize());
        assertFalse(response.isDirectory());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFileNotExist() {
        String relPath = "notfound.txt";
        Path absPath = Path.of("/root/notfound.txt");

        when(validator.toAbsolute(relPath)).thenReturn(absPath);
        when(validator.toRelative(absPath.toString())).thenReturn(relPath);
        when(storage.getFileInfo(absPath)).thenThrow(new NotFoundException("File not found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> manager.getFileInfo(relPath));
        assertEquals("File not found", ex.getMessage());
    }

    @Test
    void shouldThrowIfPathOutsideRoot() {
        String relPath = "../../etc/passwd";

        when(validator.toAbsolute(relPath)).thenThrow(new NotFoundException("outside root"));

        assertThrows(NotFoundException.class, () -> manager.getFileInfo(relPath));
    }
}
