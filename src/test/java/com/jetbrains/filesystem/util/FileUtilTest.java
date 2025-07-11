package com.jetbrains.filesystem.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void deletePath_shouldDeleteNestedFilesAndFolders() throws IOException {
        Path dir = tempDir.resolve("nested");
        Path file1 = dir.resolve("a.txt");
        Path file2 = dir.resolve("sub/b.txt");

        Files.createDirectories(file2.getParent());
        Files.writeString(file1, "hello");
        Files.writeString(file2, "world");

        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));

        FileUtil.deletePath(dir);

        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));
        assertFalse(Files.exists(dir));
    }

    @Test
    void copyPath_shouldCopyFolderStructureAndFiles() throws IOException {
        Path srcDir = tempDir.resolve("src");
        Path dstDir = tempDir.resolve("dst");

        Path file1 = srcDir.resolve("a.txt");
        Path file2 = srcDir.resolve("sub/b.txt");

        Files.createDirectories(file2.getParent());
        Files.writeString(file1, "hello");
        Files.writeString(file2, "world");

        FileUtil.copyPath(srcDir, dstDir);

        assertEquals("hello", Files.readString(dstDir.resolve("a.txt")));
        assertEquals("world", Files.readString(dstDir.resolve("sub/b.txt")));
    }

    @Test
    void readFileToBuffer_shouldReadSmallFileIntoHeapBuffer() throws IOException {
        Path file = tempDir.resolve("small.txt");
        Files.writeString(file, "test buffer");

        ByteBuffer buffer = FileUtil.readFileToBuffer(file, 1024 * 1024); // mmapThreshold 1MB
        String content = new String(buffer.array(), StandardCharsets.UTF_8).trim();

        assertEquals("test buffer", content);
    }

    @Test
    void readFileToBuffer_shouldReadWithOffsetAndLength() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file, "ABCDEFGHIJ");

        ByteBuffer buffer = FileUtil.readFileToBuffer(file, 2, 4, 1024 * 1024);
        byte[] read = new byte[buffer.remaining()];
        buffer.get(read);
        String str = new String(read, StandardCharsets.UTF_8);

        assertEquals("CDEF", str);
    }

    @Test
    void readFileToBuffer_shouldThrowIfOffsetTooLarge() throws IOException {
        Path file = tempDir.resolve("fail.txt");
        Files.writeString(file, "short");

        assertThrows(IllegalArgumentException.class, () ->
                FileUtil.readFileToBuffer(file, 100, 10, 1024));
    }

    @Test
    void encodeBase64_shouldReturnExpectedBase64() {
        String input = "ChatGPT";
        ByteBuffer buffer = ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8));

        String base64 = FileUtil.encodeBase64(buffer);
        assertEquals("Q2hhdEdQVA==", base64);
    }
}
