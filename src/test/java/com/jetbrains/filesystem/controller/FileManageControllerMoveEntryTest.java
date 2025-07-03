package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.dto.JsonRpcRequest;
import com.jetbrains.filesystem.service.FileManageService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;

import java.io.FileNotFoundException;
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
@AutoConfigureMockMvc
public class FileManageControllerMoveEntryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileServiceProperties properties;

    @Autowired
    private FileManageService fileService;

    @TestConfiguration
    static class SpyConfig {

        @Autowired
        private FileServiceProperties properties;

        @Bean
        public FileManageService fileManageService() {
            return spy(new FileManageService(properties));  // ✅ 显式注入 spy 实例
        }
    }

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

    private String toJsonRpc(String sourcePath, String targetPath, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sourcePath", sourcePath);
        params.put("targetPath", targetPath);
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("moveEntry");
        request.setId(id);
        request.setParams(params);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void testMoveEntry_Success() throws Exception {
        String source = "test-folder/original.txt";
        String target = "test-folder/moved.txt";

        Path sourcePath = root.resolve(source);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "test data");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, target, "case-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sourcePath").value(source))
                .andExpect(jsonPath("$.result.targetPath").value(target))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.id").value("case-1"));

        Assertions.assertFalse(Files.exists(sourcePath));
        Assertions.assertTrue(Files.exists(root.resolve(target)));
    }

    @Test
    void testMoveEntry_SourceOutsideRoot_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("../../outside.txt", "safe/target.txt", "case-2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-2"));
    }

    @Test
    void testMoveEntry_TargetOutsideRoot_ShouldReturnError() throws Exception {
        String source = "test-folder/file.txt";
        Path sourcePath = root.resolve(source);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "test");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, "../../etc/passwd", "case-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    void testMoveEntry_SourceNotFound_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("test-folder/missing.txt", "test-folder/target.txt", "case-4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("File not found")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }

    @Test
    void testMoveEntry_TargetAlreadyExists_ShouldReturnError() throws Exception {
        String source = "test-folder/src.txt";
        String target = "test-folder/exist.txt";
        Path sourcePath = root.resolve(source);
        Path targetPath = root.resolve(target);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "data");
        Files.writeString(targetPath, "existing");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, target, "case-5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("target already exists")))
                .andExpect(jsonPath("$.id").value("case-5"));
    }

    @Test
    void testMoveEntry_TargetIsChildOfSource_ShouldReturnError() throws Exception {
        String source = "test-folder/parent";
        String target = "test-folder/parent/child";

        Path sourcePath = root.resolve(source);
        Files.createDirectories(sourcePath);

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, target, "case-6")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("own subdirectories")))
                .andExpect(jsonPath("$.id").value("case-6"));
    }

    @Test
    void testMoveEntry_MoveFails_ShouldReturnIOException() throws Exception {
        String source = "test-folder/broken.txt";
        String target = "test-folder/target.txt";

        Path sourcePath = root.resolve(source);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "test");

        doThrow(new IOException("Simulated move failure"))
                .when(fileService).movePath(any(Path.class), any(Path.class));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, target, "case-7")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32001))
                .andExpect(jsonPath("$.error.message").value(containsString("Failed to move")))
                .andExpect(jsonPath("$.id").value("case-7"));
    }
}
