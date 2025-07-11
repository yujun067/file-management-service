package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jetbrains.filesystem.dto.rpc.JsonRpcRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FileManageControllerGetFileInfoTest extends AbstractFileManageControllerTest {
    private String toJsonRpc(String path, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);

        JsonNode paramsNode = objectMapper.valueToTree(params);
        JsonNode idNode = objectMapper.readTree("\"" + id + "\"");

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("getFileInfo");
        request.setId(idNode);
        request.setParams(paramsNode);
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
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("outside root")))
                .andExpect(jsonPath("$.id").value("case-2"));
    }

    @Test
    public void testGetFileInfo_NullPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc(null, "case-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Invalid params")))
                .andExpect(jsonPath("$.id").value("case-3"));
    }

    @Test
    public void testGetFileInfo_EmptyPath_ShouldReturnError() throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonRpc("", "case-4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("Invalid params")))
                .andExpect(jsonPath("$.id").value("case-4"));
    }
}
