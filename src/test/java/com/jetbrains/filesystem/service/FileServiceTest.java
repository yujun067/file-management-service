package com.jetbrains.filesystem.service;

import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FileServiceTest {
    @Autowired
    private FileManageService fileManageService;
    @Autowired
    private FileServiceProperties fileServiceProperties;

    private Path root;

    @BeforeEach
    void setup() {
        root = Paths.get(fileServiceProperties.getRootFolder()).toAbsolutePath().normalize();
    }

    @AfterEach
    void tearDown() throws IOException {
        Path testDir = root.resolve("test-folder");
        if (Files.exists(testDir)) {
            deleteRecursively(testDir);
        }

        Path testDir2 = root.resolve("test-folder2");
        if (Files.exists(testDir2)) {
            deleteRecursively(testDir2);
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        if (Files.isDirectory(path)) {
            try (Stream<Path> paths = Files.list(path)) {
                for (Path child : paths.toList()) {
                    deleteRecursively(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    @Test
    void testGetFileInfo() throws Exception {
        String testFilePath = "test-folder/file01.txt";
        Path path = root.resolve(testFilePath);

        Files.createDirectories(path.getParent());
        Files.createFile(path);
        Files.writeString(path, "Hello World");

        GetFileInfoResponse response = fileManageService.getFileInfo(testFilePath);

        assertEquals("file01.txt", response.getName());
        assertEquals(testFilePath, response.getPath());
        assertTrue(response.getSize()>0);
    }

    @Test
    void testListDirectoryChildren() throws Exception {
        String testDir = "test-folder";
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

    @Test
    void testCreateFolderRecursively() throws Exception {
        String testFolder = "test-folder/subfolder";
        Path dirPath = Paths.get(fileServiceProperties.getRootFolder(),testFolder);

        CreateEntryResponse response = fileManageService.createEntry(testFolder,"folder");
        //System.out.println(response.toString());
        assertEquals("subfolder", response.getName());
        assertEquals(testFolder, response.getPath());
        assertTrue(response.isDirectory());
        Path newPath = Paths.get(fileServiceProperties.getRootFolder(),testFolder);
        assertTrue(Files.exists(newPath));
    }

    @Test
    void testCreateFileRecursively() throws Exception {
        String testFile = "test-folder/file03.txt";

        CreateEntryResponse response = fileManageService.createEntry(testFile,"file");
        //System.out.println(response.toString());
        assertEquals("file03.txt", response.getName());
        assertEquals(testFile, response.getPath());
        assertFalse(response.isDirectory());
        Path newPath = Paths.get(fileServiceProperties.getRootFolder(),testFile);
        assertTrue(Files.exists(newPath));
    }

    @Test
    void testDeleteFileRecursively() throws Exception {
        //add a file and a sub folder, to test recursive deletion.
        String testFolder = "test-folder/file04.txt";
        Path dirPath = Paths.get(fileServiceProperties.getRootFolder(),testFolder);
        Files.createDirectories(dirPath.getParent());
        Files.writeString(dirPath, "Hello World");
        Path subFolderPath = dirPath.getParent().resolve("subfolder04").normalize();
        Files.createDirectories(subFolderPath);

        DeleteEntryResponse response = fileManageService.deleteEntry("test-folder");
        //System.out.println(response.toString());
        assertFalse(Files.exists(dirPath.getParent()));
    }

    @Test
    void testMoveFile() throws Exception {
        String source = "test-folder/file05.txt";
        String target = "test-folder2/file06.txt";
        Path sourcePath = Paths.get(fileServiceProperties.getRootFolder(),source);
        Path targetPath = Paths.get(fileServiceProperties.getRootFolder(),target);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "Hello World");

        MoveEntryResponse response = fileManageService.moveEntry(source,target);
        assertEquals(source,response.getSourcePath());
        assertEquals(target,response.getTargetPath());
        assertFalse(Files.exists(sourcePath));
        assertTrue(Files.exists(targetPath));
    }


    @Test
    void testCopyFile() throws Exception {
        String source = "test-folder/file05.txt";
        String target = "test-folder2/file06.txt";
        Path sourcePath = Paths.get(fileServiceProperties.getRootFolder(),source);
        Path targetPath = Paths.get(fileServiceProperties.getRootFolder(),target);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "Hello World");

        CopyEntryResponse response = fileManageService.copyEntry(source,target);
        assertEquals(source,response.getSourcePath());
        assertEquals(target,response.getTargetPath());
        assertTrue(Files.exists(sourcePath));
        assertTrue(Files.exists(targetPath));
    }

    @Test
    void testReadFileSegment() throws Exception {
        String testFile = "test-folder/file07.txt";
        Path filePath = Paths.get(fileServiceProperties.getRootFolder(), testFile);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "Hello World");

        ReadFileSegmentResponse response = fileManageService.readFile(testFile, 0, 5);
        //System.out.println(response.getData().length());
        String decoded = new String(Base64.getDecoder().decode(response.getData()), StandardCharsets.UTF_8);
        assertEquals("Hello", decoded);
    }

    @Test
    void testAppendData() throws Exception {
        String testFile = "test-folder/file08.txt";
        Path filePath = Paths.get(fileServiceProperties.getRootFolder(), testFile);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "Hello World");

        //append base64 encoded "123"
        fileManageService.appendDataToFile(testFile, "MTIz");
        String content = Files.readString(filePath);
        assertEquals("Hello World123", content);
    }

    @Test
    void testAppendDataConcurrently() throws Exception {
        String testFile = "test-folder/file08.txt";
        Path filePath = Paths.get(fileServiceProperties.getRootFolder(), testFile);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int taskNum = 100;
        CountDownLatch latch = new CountDownLatch(taskNum);
        for(int i=0;i<taskNum;i++) {
            int count = i;
            executor.submit(()->{
               try {
                   String originData = "task"+count;
                   String encodedData = Base64.getEncoder().encodeToString(originData.getBytes(StandardCharsets.UTF_8));
                   fileManageService.appendDataToFile(testFile, encodedData);
               } catch (Exception e) {
                   e.printStackTrace();
               } finally {
                   latch.countDown();
               }
            });
        }

        latch.await();
        executor.shutdown();
        String content = Files.readString(filePath);
        //System.out.println(content);
        for (int i = 0; i < taskNum; i++) {
            assertTrue(content.contains("task"+i));
        }
    }

}
