package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jetbrains.filesystem.dto.rpc.JsonRpcRequest;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.nio.file.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FileManageControllerReadFileSegmentTest extends AbstractFileManageControllerTest {
    private String toJsonRpc(String path, long offset, int length, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("offset", offset);
        params.put("length", length);

        JsonNode paramsNode = objectMapper.valueToTree(params);
        JsonNode idNode = objectMapper.readTree("\"" + id + "\"");

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("readFileSegment");
        request.setId(idNode);
        request.setParams(paramsNode);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void testReadFileSegment_Success() throws Exception {
        String path = "test-folder/sample.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "hello");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, 0, 5, "case-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data").value(Base64.getEncoder().encodeToString("hello".getBytes())))
                .andExpect(jsonPath("$.id").value("case-1"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void testReadFileSegment_PathOutsideRoot_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("../../etc/passwd", 0, 10, "case-2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("outside root")))
                .andExpect(jsonPath("$.id").value("case-2"));
    }

    @Test
    void testReadFileSegment_FileNotExist_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("nonexistent.txt", 0, 10, "case-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("File not found")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    void testReadFileSegment_PathIsDirectory_ShouldReturnError() throws Exception {
        String path = "test-folder/dir";
        Files.createDirectories(root.resolve(path));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, 0, 10, "case-4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("File not found")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }

    @Test
    void testReadFileSegment_NegativeOffset_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("test-folder/file.txt", -5L, 10, "case-5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("offset must be in range")))
                .andExpect(jsonPath("$.id").value("case-5"));
    }

    @Test
    void testReadFileSegment_ZeroOrNegativeLength_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("test-folder/file.txt", 0, 0, "case-6")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("length must be positive")))
                .andExpect(jsonPath("$.id").value("case-6"));

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("test-folder/file.txt", 0, -1, "case-7")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("length must be positive")))
                .andExpect(jsonPath("$.id").value("case-7"));
    }

    @Test
    void testReadFileSegment_OffsetExceedsFileLength_ShouldReturnError() throws Exception {
        String path = "test-folder/short.txt";
        Path filePath = root.resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "abcde");

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(path, 100, 10, "case-8")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("offset must be in range")))
                .andExpect(jsonPath("$.id").value("case-8"));
    }
}
