package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jetbrains.filesystem.dto.rpc.JsonRpcRequest;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FileManageControllerListDirectoryChildrenTest extends AbstractFileManageControllerTest {
    private String toJsonRpc(String id, String path) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);

        JsonNode paramsNode = objectMapper.valueToTree(params);
        JsonNode idNode = objectMapper.readTree("\"" + id + "\"");

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("listDirectoryChildren");
        request.setId(idNode);
        request.setParams(paramsNode);
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
                        .content(toJsonRpc("case-0", testDir)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.jsonrpc").value("2.0"),
                        jsonPath("$.id").value("case-0"),
                        jsonPath("$.error").doesNotExist(),

                        // 3 elements
                        jsonPath("$.result.fileInfos", hasSize(3)),
                        jsonPath("$.result.fileInfos[*].name",
                                containsInAnyOrder("file1.txt", "file2.txt", "subdir"))
                );
    }

    @Test
    void testListDirectoryChildren_PathOutsideRoot_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("case-1", "../../etc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("outside root")))
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
                        .content(toJsonRpc("case-2", filePath)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("File not found")))
                .andExpect(jsonPath("$.id").value("case-2"));
    }

    @Test
    void testListDirectoryChildren_NonExistentPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("case-3", "nonexistent-folder")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("File not found")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    void testListDirectoryChildren_NullPath_ShouldReturnError() throws Exception {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("listDirectoryChildren");
        JsonNode idNode = objectMapper.readTree("\"case-4\"");
        request.setId(idNode);
        Map<String, Object> params = new HashMap<>();
        params.put("path", null);
        JsonNode paramsNode = objectMapper.valueToTree(params);
        request.setParams(paramsNode);

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Invalid params")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }

    @Test
    void testListDirectoryChildren_EmptyPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("case-5", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Invalid params")))
                .andExpect(jsonPath("$.id").value("case-5"));
    }
}

