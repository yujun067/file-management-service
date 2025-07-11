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

public class FileManageControllerCreateEntryTest extends AbstractFileManageControllerTest{
    private String toJsonRpc(String id, String path, String type) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("type", type);

        JsonNode paramsNode = objectMapper.valueToTree(params);
        JsonNode idNode = objectMapper.readTree("\"" + id + "\"");

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("createEntry");
        request.setId(idNode);
        request.setParams(paramsNode);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void testCreateEntry_InvalidType_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("case-1", "test-folder/invalid.txt", "badtype")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("Unknown type")))
                .andExpect(jsonPath("$.id").value("case-1"));
    }

    @Test
    void testCreateEntry_PathOutsideRoot_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("case-2", "../../etc/passwd", "file")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("outside root")))
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
                        .content(toJsonRpc("case-3", path, "file")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(containsString("already exists")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }


    @Test
    void testCreateEntry_Success_CreateFile() throws Exception {
        String path = "test-folder/created-file.txt";

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("case-5", path, "file")))
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
                        .content(toJsonRpc("case-6", path, "folder")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("new-folder"))
                .andExpect(jsonPath("$.result.path").value(path))
                .andExpect(jsonPath("$.result.directory").value(true))
                .andExpect(jsonPath("$.id").value("case-6"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }
}
