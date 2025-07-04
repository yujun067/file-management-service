package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.dto.JsonRpcRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileManageControllerGetFileInfoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileServiceProperties fileServiceProperties;

    private String endpoint = "/filemanage";

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

    private String toJsonRpc(String path, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("getFileInfo");
        request.setId(id);
        request.setParams(params);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    public void testGetFileInfo_Success() throws Exception {
        String testFilePath = "test-folder/file01.txt";
        Path path = root.resolve(testFilePath);
        Files.createDirectories(path.getParent());
        Files.createFile(path);
        Files.writeString(path, "Hello World");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(testFilePath, "case-0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("file01.txt"))
                .andExpect(jsonPath("$.result.path").value(testFilePath))
                .andExpect(jsonPath("$.result.size").value(11))
                .andExpect(jsonPath("$.id").value("case-0"))
                .andExpect(jsonPath("$.error").doesNotExist());

    }

    @Test
    public void testGetFileInfo_FileNotFound_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("nonexistent-file.txt", "case-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("File not found")))
                .andExpect(jsonPath("$.id").value("case-1"));
    }

    @Test
    public void testGetFileInfo_InvalidPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("../malicious.txt", "case-2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-2"));
    }

    @Test
    public void testGetFileInfo_NullPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(null, "case-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Invalid path")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    public void testGetFileInfo_EmptyPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("", "case-4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Invalid path")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }
}
