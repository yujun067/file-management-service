package com.jetbrains.filesystem.util;

import com.jetbrains.filesystem.exception.ConflictException;
import com.jetbrains.filesystem.exception.NotFoundException;
import com.jetbrains.filesystem.exception.ValidationException;
import com.jetbrains.filesystem.config.FileServiceProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.*;

@Component
public class PathValidator {
    private final FileServiceProperties props;
    private final Path rootPath;

    public PathValidator(FileServiceProperties props) {
        this.props = props;
        this.rootPath = Paths.get(props.getRootFolder()).toAbsolutePath().normalize();
    }

    public Path toAbsolute(String relative) {
        if (relative == null || relative.isBlank())
            throw new ValidationException("path empty");
        Path target = rootPath.resolve(relative).normalize();
        if (!target.startsWith(rootPath))
            throw new ValidationException("outside root");
        return target;
    }

    public void validatePath(Path source, Path target) {
        if (!source.startsWith(rootPath)||!target.startsWith(rootPath)) {
            throw new ValidationException("outside root");
        }
        if(!Files.exists(source)){
            throw new NotFoundException("source not found");
        }
        //prevent self-contain
        if (target.startsWith(source)) {
            throw new ConflictException("own subdirectories");
        }
        if(Files.exists(target)){
            throw new ConflictException("target already exists");
        }
    }

    public String toRelative(String absPath) {
        Path absolutePath = Paths.get(absPath).toAbsolutePath().normalize();
        if (!absolutePath.startsWith(rootPath)) {
            throw new ValidationException("outside root");
        }

        Path relative = rootPath.relativize(absolutePath);
        return relative.toString().replace(File.separatorChar, '/');
    }

    public void validateSourceForAppend(Path source) {
        File sourceFile = source.toFile();
        if(!sourceFile.exists()){
            throw new NotFoundException("File not found");
        }
        if(!sourceFile.isFile()){
            throw new ValidationException("Can't append to a directory");
        }
    }

}

