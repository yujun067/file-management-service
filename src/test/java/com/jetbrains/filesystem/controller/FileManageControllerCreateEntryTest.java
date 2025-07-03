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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileManageControllerCreateEntryTest {

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

    private String toJsonRpc(String method, String id, String path, String type) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("type", type);
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod(method);
        request.setId(id);
        request.setParams(params);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void testCreateEntry_InvalidType_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("createEntry", "case-1", "test-folder/invalid.txt", "badtype")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("type must be 'file' or 'folder'")))
                .andExpect(jsonPath("$.id").value("case-1"));
    }

    @Test
    void testCreateEntry_PathOutsideRoot_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("createEntry", "case-2", "../../etc/passwd", "file")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-2"));
    }

    @Test
    void testCreateEntry_PathAlreadyExists_ShouldReturnError() throws Exception {
        String path = "test-folder/existing.txt";
        Path fullPath = root.resolve(path);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, "exists");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("createEntry", "case-3", path, "file")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("already exists")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    void testCreateEntry_CreationFails_ShouldReturnIOException() throws Exception {
        String path = "test-folder/no-permission/file.txt";
        Path parentDir = root.resolve("test-folder/no-permission");
        Files.createDirectories(parentDir);
        parentDir.toFile().setWritable(false);

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("createEntry", "case-4", path, "file")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32001))
                .andExpect(jsonPath("$.error.message").value(containsString("Failed to create")))
                .andExpect(jsonPath("$.id").value("case-4"));

        parentDir.toFile().setWritable(true);
    }

    @Test
    void testCreateEntry_Success_CreateFile() throws Exception {
        String path = "test-folder/created-file.txt";

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("createEntry", "case-5", path, "file")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("created-file.txt"))
                .andExpect(jsonPath("$.result.path").value(path))
                .andExpect(jsonPath("$.result.directory").value(false))
                .andExpect(jsonPath("$.id").value("case-5"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void testCreateEntry_Success_CreateFolder() throws Exception {
        String path = "test-folder/new-folder";

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("createEntry", "case-6", path, "folder")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("new-folder"))
                .andExpect(jsonPath("$.result.path").value(path))
                .andExpect(jsonPath("$.result.directory").value(true))
                .andExpect(jsonPath("$.id").value("case-6"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }
}
