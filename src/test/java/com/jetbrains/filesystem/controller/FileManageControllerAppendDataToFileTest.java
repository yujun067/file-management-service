package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jetbrains.filesystem.dto.rpc.JsonRpcRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FileManageControllerAppendDataToFileTest extends  AbstractFileManageControllerTest{
    protected String toJsonRpc(String path, String base64Data, String idString) throws Exception {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("path", path);
        paramsMap.put("data", base64Data);

        JsonNode paramsNode = objectMapper.valueToTree(paramsMap);
        JsonNode idNode = objectMapper.readTree("\"" + idString + "\"");

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("appendDataToFile");
        request.setParams(paramsNode);
        request.setId(idNode);

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
                .andExpect(jsonPath("$.error.message").value(containsString("outside root")))
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
                .andExpect(jsonPath("$.error.message").value(containsString("Invalid params")))
                .andExpect(jsonPath("$.id").value("case-5"));
    }

}
