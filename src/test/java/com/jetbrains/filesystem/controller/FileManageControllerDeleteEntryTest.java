package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.filesystem.TestSpyConfig;
import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.dto.JsonRpcRequest;

import com.jetbrains.filesystem.service.FileManageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestSpyConfig.class)
@AutoConfigureMockMvc
public class FileManageControllerDeleteEntryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileServiceProperties properties;

    @Autowired
    private FileManageService fileService;

    private Path root;

    private final String endpoint = "/filemanage";

    @BeforeEach
    void setup() {
        root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
    }

    @AfterEach
    void tearDown() throws IOException {
        reset(fileService);  // clear configurations like throws
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
        request.setMethod("deleteEntry");
        request.setId(id);
        request.setParams(params);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void testDeleteEntry_Success_File() throws Exception {
        String path = "test-folder/testfile.txt";
        Path fullPath = root.resolve(path);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, "delete me");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, "case-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.path").value(path))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.id").value("case-1"));

        Assertions.assertFalse(Files.exists(fullPath));
    }

    @Test
    void testDeleteEntry_Success_EmptyFolder() throws Exception {
        String path = "test-folder/empty-dir";
        Path fullPath = root.resolve(path);
        Files.createDirectories(fullPath);

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, "case-2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.path").value(path))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.id").value("case-2"));

        Assertions.assertFalse(Files.exists(fullPath));
    }

    @Test
    void testDeleteEntry_PathOutsideRoot_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("../../outside.txt", "case-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    void testDeleteEntry_NonExistentFile_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("non-existent.txt", "case-4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("not found")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }

    @Test
    void testDeleteEntry_DeleteFails_ShouldReturnIOException() throws Exception {
        String path = "test-folder/delete-fail.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "test");

        // mock deletePath throws IOException
        doThrow(new IOException("Simulated failure")).when(fileService).deletePath(any(Path.class));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, "case-5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32001))
                .andExpect(jsonPath("$.error.message").value(containsString("Failed to delete")))
                .andExpect(jsonPath("$.id").value("case-5"));
    }
}

