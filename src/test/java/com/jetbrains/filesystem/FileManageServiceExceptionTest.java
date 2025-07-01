package com.jetbrains.filesystem;

import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.dto.GetFileInfoResponse;
import com.jetbrains.filesystem.service.FileManageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class FileManageServiceExceptionTest {
    @Autowired
    private FileManageService fileManageService;
    @Autowired
    private FileServiceProperties fileServiceProperties;

    @Test
    void testGetFileInfo_FileNotExists() throws Exception {
        String newFilePath = "test01.txt";
        Path path = Paths.get(fileServiceProperties.getRootFolder(),newFilePath);
        Files.createDirectories(path.getParent());
        //ensure file doesn't exist
        Files.deleteIfExists(path);

        try {
            GetFileInfoResponse response = fileManageService.getFileInfo(newFilePath);
        } catch (Exception e) {
            assertTrue(e instanceof FileNotFoundException);
        }
    }

}
