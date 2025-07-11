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

public class FileManageControllerMoveEntryTest extends AbstractFileManageControllerTest{
    private String toJsonRpc(String sourcePath, String targetPath, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sourcePath", sourcePath);
        params.put("targetPath", targetPath);

        JsonNode paramsNode = objectMapper.valueToTree(params);
        JsonNode idNode = objectMapper.readTree("\"" + id + "\"");

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("moveEntry");
        request.setId(idNode);
        request.setParams(paramsNode);
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
                .andExpect(jsonPath("$.error.message").value(containsString("outside root")))
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
                .andExpect(jsonPath("$.error.message").value(containsString("outside root")))
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
                .andExpect(jsonPath("$.error.code").value(-32002))
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
                .andExpect(jsonPath("$.error.code").value(-32002))
                .andExpect(jsonPath("$.error.message").value(containsString("own subdirectories")))
                .andExpect(jsonPath("$.id").value("case-6"));
    }
}
