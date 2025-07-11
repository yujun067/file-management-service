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

public class FileManageControllerCopyEntryTest extends AbstractFileManageControllerTest{
    private String toJsonRpc(String sourcePath, String targetPath, String id) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sourcePath", sourcePath);
        params.put("targetPath", targetPath);

        JsonNode paramsNode = objectMapper.valueToTree(params);
        JsonNode idNode = objectMapper.readTree("\"" + id + "\"");

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("copyEntry");
        request.setId(idNode);
        request.setParams(paramsNode);
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
                .andExpect(jsonPath("$.error.message").value(containsString("outside root")))
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
                .andExpect(jsonPath("$.error.message").value(containsString("outside root")))
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
                .andExpect(jsonPath("$.error.code").value(-32002))
                .andExpect(jsonPath("$.error.message").value(containsString("target already exists")))
                .andExpect(jsonPath("$.id").value("case-6"));
    }
}
