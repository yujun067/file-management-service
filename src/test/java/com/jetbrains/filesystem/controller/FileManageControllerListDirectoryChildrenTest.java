package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.filesystem.dto.JsonRpcRequest;
import com.jetbrains.filesystem.config.FileServiceProperties;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileManageControllerListDirectoryChildrenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileServiceProperties properties;

    private Path root;

    private final String endpoint = "/filemanage";

    @BeforeEach
    void setup() {
        root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
    }

    @AfterEach
    void tearDown() throws IOException {
        Path testDir = root.resolve("test-folder");
        if (Files.exists(testDir)) {
            deleteRecursively(testDir);
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

    private String toJsonRpc(String method, String id, String path) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod(method);
        request.setId(id);
        request.setParams(params);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void testListDirectoryChildren_Success() throws Exception {
        // Arrange
        String testDir = "test-folder";
        Path dirPath = root.resolve(testDir);
        Files.createDirectories(dirPath);

        // add 2 files and 1 dir
        Files.writeString(dirPath.resolve("file1.txt"), "content1");
        Files.writeString(dirPath.resolve("file2.txt"), "content2");
        Files.createDirectory(dirPath.resolve("subdir"));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("listDirectoryChildren", "case-0", testDir)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(3)) // file1, file2, subdir
                .andExpect(jsonPath("$.result[?(@.name=='file1.txt')]").exists())
                .andExpect(jsonPath("$.result[?(@.name=='file2.txt')]").exists())
                .andExpect(jsonPath("$.result[?(@.name=='subdir')]").exists())
                .andExpect(jsonPath("$.id").value("case-0"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void testListDirectoryChildren_PathOutsideRoot_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("listDirectoryChildren", "case-1", "../../etc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-1"));
    }

    @Test
    void testListDirectoryChildren_FileNotDirectory_ShouldReturnError() throws Exception {
        String filePath = "test-folder/not-a-dir.txt";
        Path fullPath = root.resolve(filePath);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, "this is a file");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("listDirectoryChildren", "case-2", filePath)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("not a directory")))
                .andExpect(jsonPath("$.id").value("case-2"));
    }

    @Test
    void testListDirectoryChildren_NonExistentPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("listDirectoryChildren", "case-3", "nonexistent-folder")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("not a directory")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    void testListDirectoryChildren_NullPath_ShouldReturnError() throws Exception {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("listDirectoryChildren");
        request.setId("case-4");
        Map<String, Object> params = new HashMap<>();
        params.put("path", null);
        request.setParams(params);

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Invalid path")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }

    @Test
    void testListDirectoryChildren_EmptyPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("listDirectoryChildren", "case-5", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Invalid path")))
                .andExpect(jsonPath("$.id").value("case-5"));
    }
}

