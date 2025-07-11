package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jetbrains.filesystem.dto.rpc.JsonRpcRequest;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FileManageControllerDeleteEntryTest extends AbstractFileManageControllerTest {
    private String toJsonRpc(String path, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);

        JsonNode paramsNode = objectMapper.valueToTree(params);
        JsonNode idNode = objectMapper.readTree("\"" + id + "\"");

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("deleteEntry");
        request.setId(idNode);
        request.setParams(paramsNode);
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
                .andExpect(jsonPath("$.error.message").value(containsString("outside root")))
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

}

