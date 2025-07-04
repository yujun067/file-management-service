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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
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
public class FileManageControllerAppendDataToFileTest {

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

    private String toJsonRpc(String path, String base64Data, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("data", base64Data);
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("appendDataToFile");
        request.setId(id);
        request.setParams(params);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void testAppendDataToFile_Success() throws Exception {
        String path = "test-folder/append.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "hello ");

        String base64Data = Base64.getEncoder().encodeToString("world".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, base64Data, "case-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.path").value(path))
                .andExpect(jsonPath("$.result.appendLength").value(5))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.id").value("case-1"));

        String content = Files.readString(filePath);
        Assertions.assertEquals("hello world", content);
    }

    @Test
    void testAppendDataToFile_PathOutsideRoot_ShouldReturnError() throws Exception {
        String path = "../../outside.txt";
        String base64 = Base64.getEncoder().encodeToString("test".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, base64, "case-2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Outside root folder")))
                .andExpect(jsonPath("$.id").value("case-2"));
    }

    @Test
    void testAppendDataToFile_FileDoesNotExist_ShouldReturnError() throws Exception {
        String path = "nonexistent.txt";
        String base64 = Base64.getEncoder().encodeToString("test".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, base64, "case-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("File not found")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    void testAppendDataToFile_PathIsDirectory_ShouldReturnError() throws Exception {
        String path = "test-folder/dir";
        Files.createDirectories(root.resolve(path));
        String base64 = Base64.getEncoder().encodeToString("abc".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, base64, "case-4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Can't append to a directory")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }

    @Test
    void testAppendDataToFile_InvalidBase64_ShouldReturnError() throws Exception {
        String path = "test-folder/invalid.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "");

        String invalidBase64 = "!!!$$%%INVALID==";

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, invalidBase64, "case-5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("failed to append file content")))
                .andExpect(jsonPath("$.id").value("case-5"));
    }

    @Test
    void testAppendDataToFile_WriteFails_ShouldReturnIOException() throws Exception {
        String path = "test-folder/protected.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "start");

        String base64 = Base64.getEncoder().encodeToString("abc".getBytes(StandardCharsets.UTF_8));

        doThrow(new IOException("no permission to write"))
                .when(fileService).appendFileContent(any(Path.class), any(File.class), any(String.class));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, base64, "case-6")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32001))
                .andExpect(jsonPath("$.error.message").value(containsString("no permission to write")))
                .andExpect(jsonPath("$.id").value("case-6"));
    }
}
