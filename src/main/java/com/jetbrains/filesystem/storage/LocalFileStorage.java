package com.jetbrains.filesystem.storage;

import com.jetbrains.filesystem.dto.file.FileInfo;
import com.jetbrains.filesystem.exception.*;
import com.jetbrains.filesystem.dto.file.EntryType;
import com.jetbrains.filesystem.util.FileUtil;

import com.jetbrains.filesystem.lock.FileLockRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.io.FileOutputStream;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Log4j2
public class LocalFileStorage implements FileStorage {
    private static final long MMAP_THRESHOLD = 10 * 1024 * 1024;

    private final FileLockRegistry locks;

    @Override
    public FileInfo getFileInfo(Path p) {
        File f = p.toFile();
        if (!f.exists()) throw new NotFoundException("File not found");
        return toInfo(f, p);
    }

    @Override
    public List<FileInfo> getFileInfolist(Path dir) {
        File directory = dir.toFile();
        if (!directory.exists()||!directory.isDirectory()) throw new NotFoundException("File not found");
        File[] files = directory.listFiles();
        if (files == null) return List.of();
        return Arrays.stream(files).map(f -> toInfo(f, dir.resolve(f.getName()))).toList();
    }

    @Override
    public void create(Path p, EntryType type) {
        try {
            if (type == EntryType.FILE) {
                Files.createDirectories(p.getParent());
                Files.createFile(p);
            } else {
                Files.createDirectories(p);
            }
        } catch (FileAlreadyExistsException e) {
            throw new ConflictException("already exists: " + p);
        } catch (IOException e) {
            throw new FileServiceException("File create failed", e) {};
        }
    }

    @Override
    public void delete(Path path) {
        if(!Files.exists(path)){
            throw new NotFoundException("File not found");
        }

        try {
            FileUtil.deletePath(path);
            log.debug("Deleted path: {}", path);
        } catch (IOException e) {
            throw new FileOperationException("Failed to delete", e);
        }
    }

    @Override
    public void move(Path source, Path target) {
        try {
            //ensure the target's parent folder exists
            Files.createDirectories(target.getParent());

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileOperationException("Failed to move file to target", e);
        }
    }

    @Override
    public void copy(Path source, Path target) {
        try {
            //ensure the target's parent folder exists
            Files.createDirectories(target.getParent());
            FileUtil.copyPath(source, target);
        } catch (IOException e) {
            throw new FileOperationException("Failed to copy file to target", e);
        }
    }

    @Override
    public ByteBuffer read(Path source, long offset, int length) {
        File sourceFile = source.toFile();
        validateReadRequest(sourceFile, offset, length);

        try {
            return FileUtil.readFileToBuffer(source, offset, length, MMAP_THRESHOLD);
        } catch (IOException e) {
            throw new FileOperationException("Failed to read file", e);
        }
    }

    @Override
    public int append(Path source, byte[] data) {
        ReentrantLock lock = locks.lock(source.toString());
        lock.lock();
        try (FileOutputStream fos = new FileOutputStream(source.toFile(), true)) {
            fos.write(data);
        } catch (IllegalArgumentException e) {
            throw new FileOperationException("Failed to decode base64 data for append", e);
        } catch (IOException e) {
            throw new FileOperationException("I/O error while appending file content", e);
        } finally {
            lock.unlock();
        }
        return data.length;
    }

    private FileInfo toInfo(File f, Path absolutePath) {
        return new FileInfo(
                f.getName(),
                absolutePath.toString(),
                f.length(),
                f.isDirectory()
        );
    }

    private void validateReadRequest(File sourceFile, long offset, int length) {
        if (length <= 0) {
            throw new ValidationException("length must be positive");
        }
        if (offset < 0 || offset > sourceFile.length()) {
            throw new ValidationException("offset must be in range [0," + sourceFile.length() + ")");
        }
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            throw new NotFoundException("File not found");
        }
    }

}
