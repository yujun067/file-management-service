package com.jetbrains.filesystem;

import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.dto.FileInfo;
import com.jetbrains.filesystem.dto.GetFileInfoResponse;
import com.jetbrains.filesystem.service.FileManageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class FileManageServiceTest {
    @Autowired
    private FileManageService fileManageService;
    @Autowired
    private FileServiceProperties fileServiceProperties;

    @Test
    void testGetFileInfo_FileExists() throws Exception {
        String testFilePath = "fold01/file01.txt";
        Path path = Paths.get(fileServiceProperties.getRootFolder(),testFilePath);
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
        Files.createFile(path);
        Files.writeString(path, "Hello World");

        GetFileInfoResponse response = fileManageService.getFileInfo(testFilePath);
        //System.out.println(response.toString());

        assertEquals("file01.txt", response.getName());
        assertEquals(testFilePath, response.getPath());
        assertTrue(response.getSize()>0);
    }

    @Test
    void testListDirectoryChildren() throws Exception {
        String testDir = "folder02";
        Path dirPath = Paths.get(fileServiceProperties.getRootFolder(),testDir);
        Files.createDirectories(dirPath);
        Files.writeString(dirPath.resolve("file02.txt"), "Hello World");
        Files.createDirectory(dirPath.resolve("subfolder02"));

        List<FileInfo> fileInfoList = fileManageService.listDirectoryChildren(testDir);

        assertEquals(2, fileInfoList.size());
        boolean hasFile = false;
        boolean hasSubfolder = false;
        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.getName().equals("file02.txt") && !fileInfo.isDirectory()) {
                hasFile = true;
            }
            if (fileInfo.getName().equals("subfolder02") && fileInfo.isDirectory()) {
                hasSubfolder = true;
            }
        }
        assertTrue(hasFile);
        assertTrue(hasSubfolder);
    }

}
