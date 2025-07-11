package com.jetbrains.filesystem.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;

public class FileUtil {
    public static void deletePath(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copyPath(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = target.resolve(source.relativize(dir));
                    Files.createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = target.resolve(source.relativize(file));
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static ByteBuffer readFileToBuffer(Path path, long mmapThreshold) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();

            if (fileSize > mmapThreshold) {
                return channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            } else {
                ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
                int bytesRead = 0;
                while (bytesRead < fileSize) {
                    int read = channel.read(buffer);
                    if (read == -1) break;
                    bytesRead += read;
                }
                buffer.flip();
                return buffer;
            }
        }
    }

    public static String encodeBase64(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static ByteBuffer readFileToBuffer(Path path, long offset, int length, long mmapThreshold) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();

            if (offset < 0 || offset >= fileSize) {
                throw new IllegalArgumentException("Invalid offset: " + offset);
            }

            long maxLength = fileSize - offset;
            int safeLength = (int) Math.min(length, maxLength); // prevent reading exceeds EOF

            if (fileSize > mmapThreshold) {
                // MMAP to read part of content
                MappedByteBuffer mappedBuffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, safeLength);
                return mappedBuffer;
            } else {
                // normal ByteBuffer to read part of content
                ByteBuffer buffer = ByteBuffer.allocate(safeLength);
                channel.position(offset);
                int bytesRead = 0;
                while (bytesRead < safeLength) {
                    int read = channel.read(buffer);
                    if (read == -1) break;
                    bytesRead += read;
                }
                buffer.flip();
                return buffer;
            }
        }
    }

}
