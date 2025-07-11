package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jetbrains.filesystem.dto.rpc.JsonRpcRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.nio.file.*;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FileManageControllerBatchTest extends AbstractFileManageControllerTest {
    private String toJsonRpc(String method, String id, String path) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);

        JsonNode paramsNode = objectMapper.valueToTree(params);
        JsonNode idNode = objectMapper.valueToTree(id);   // Automatically converts to "abc", 123, etc.

        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod(method);
        request.setId(idNode);
        request.setParams(paramsNode);
        return objectMapper.writeValueAsString(request);
    }

    /* ---------- Batch request test ---------- */

    @Test
    void testBatch_ListAndGetFileInfo_Success() throws Exception {
        // Arrange ── create test directory and files
        String testDir = "test-folder";
        Path dirPath = root.resolve(testDir);
        Files.createDirectories(dirPath);

        Files.writeString(dirPath.resolve("file1.txt"), "content1");
        Files.writeString(dirPath.resolve("file2.txt"), "content2");
        Files.createDirectory(dirPath.resolve("subdir"));

        // Build batch payload with 2 JSON-RPC requests
        String req1 = toJsonRpc("listDirectoryChildren", "case-1", testDir);
        String req2 = toJsonRpc("getFileInfo", "case-2", testDir + "/file1.txt");
        String batchJson = "[" + req1 + "," + req2 + "]";

        // Act & Assert ── send batch request and validate the response
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpectAll(
                        status().isOk(),

                        /* The whole response is a JSON array of size 2 */
                        jsonPath("$", hasSize(2)),

                        /* ---------- First response: listDirectoryChildren ---------- */
                        jsonPath("$[0].jsonrpc").value("2.0"),
                        jsonPath("$[0].id").value("case-1"),
                        jsonPath("$[0].error").doesNotExist(),
                        jsonPath("$[0].result.fileInfos", hasSize(3)),
                        jsonPath("$[0].result.fileInfos[*].name",
                                containsInAnyOrder("file1.txt", "file2.txt", "subdir")),

                        /* ---------- Second response: getFileInfo ---------- */
                        jsonPath("$[1].jsonrpc").value("2.0"),
                        jsonPath("$[1].id").value("case-2"),
                        jsonPath("$[1].error").doesNotExist(),
                        jsonPath("$[1].result.name").value("file1.txt"),
                        jsonPath("$[1].result.size").value(8)  // 8 bytes = "content1"
                );
    }


    @Test
    void testBatch_ListAndGetFileInfo_WithOneError() throws Exception {
        // Arrange ── create test directory and files
        String testDir = "test-folder";
        Path dirPath = root.resolve(testDir);
        Files.createDirectories(dirPath);

        Files.writeString(dirPath.resolve("valid.txt"), "hello");
        Files.createDirectory(dirPath.resolve("subdir"));

        // construct 3 requests：the second request has no exists file.
        String req1 = toJsonRpc("listDirectoryChildren", "req-1", testDir);
        String req2 = toJsonRpc("getFileInfo", "req-2", testDir + "/nonexistent.txt");
        String req3 = toJsonRpc("getFileInfo", "req-3", testDir + "/valid.txt");

        String batchJson = "[" + req1 + "," + req2 + "," + req3 + "]";

        // Act & Assert
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpectAll(
                        status().isOk(),

                        // expect 2 results: 1 and 2
                        jsonPath("$", hasSize(2)),

                        // req-1: listDirectoryChildren success
                        jsonPath("$[0].id").value("req-1"),
                        jsonPath("$[0].result.fileInfos", hasSize(2)),
                        jsonPath("$[0].result.fileInfos[*].name",
                                containsInAnyOrder("valid.txt", "subdir")),

                        // req-2: getFileInfo error
                        jsonPath("$[1].id").value("req-2"),
                        jsonPath("$[1].error.code").value(-32000),
                        jsonPath("$[1].error.message").value(org.hamcrest.Matchers.containsString("File not found"))
                );
    }

}
