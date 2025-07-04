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
@Import(TestSpyConfig.class)
@AutoConfigureMockMvc
public class FileManageControllerCopyEntryTest {

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

    private String toJsonRpc(String sourcePath, String targetPath, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sourcePath", sourcePath);
        params.put("targetPath", targetPath);
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("copyEntry");
        request.setId(id);
        request.setParams(params);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void testCopyEntry_Success_File() throws Exception {
        String source = "test-folder/source.txt";
        String target = "test-folder/copied.txt";

        Path sourcePath = root.resolve(source);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "copy me");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, target, "case-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sourcePath").value(source))
                .andExpect(jsonPath("$.result.targetPath").value(target))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.id").value("case-1"));

        Assertions.assertTrue(Files.exists(root.resolve(target)));
        Assertions.assertEquals("copy me", Files.readString(root.resolve(target)));
    }

    @Test
    void testCopyEntry_Success_Directory() throws Exception {
        String source = "test-folder/dir-src";
        String target = "test-folder/dir-dst";
        Path sourceDir = root.resolve(source);
        Files.createDirectories(sourceDir);
        Files.writeString(sourceDir.resolve("a.txt"), "data");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, target, "case-2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sourcePath").value(source))
                .andExpect(jsonPath("$.result.targetPath").value(target))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.id").value("case-2"));

        Assertions.assertTrue(Files.exists(root.resolve(target + "/a.txt")));
    }

    @Test
    void testCopyEntry_SourcePathOutsideRoot_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("../../outside.txt", "test-folder/target.txt", "case-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    void testCopyEntry_TargetPathOutsideRoot_ShouldReturnError() throws Exception {
        String source = "test-folder/source.txt";
        Path sourcePath = root.resolve(source);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "abc");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, "../../etc/passwd", "case-4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }

    @Test
    void testCopyEntry_SourceDoesNotExist_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("nonexistent.txt", "test-folder/target.txt", "case-5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("File not found")))
                .andExpect(jsonPath("$.id").value("case-5"));
    }

    @Test
    void testCopyEntry_TargetAlreadyExists_ShouldReturnError() throws Exception {
        String source = "test-folder/src.txt";
        String target = "test-folder/dst.txt";

        Files.createDirectories(root.resolve("test-folder"));
        Files.writeString(root.resolve(source), "abc");
        Files.writeString(root.resolve(target), "exists");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, target, "case-6")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("target already exists")))
                .andExpect(jsonPath("$.id").value("case-6"));
    }

    @Test
    void testCopyEntry_CopyFails_ShouldReturnIOException() throws Exception {
        String source = "test-folder/fail-src.txt";
        String target = "test-folder/fail-dst.txt";

        Path sourcePath = root.resolve(source);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, "data");

        doThrow(new IOException("Simulated copy failure"))
                .when(fileService).copyPath(any(Path.class), any(Path.class));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(source, target, "case-7")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32001))
                .andExpect(jsonPath("$.error.message").value(containsString("Failed to copy")))
                .andExpect(jsonPath("$.id").value("case-7"));
    }
}
