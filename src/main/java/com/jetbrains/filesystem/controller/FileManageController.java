package com.jetbrains.filesystem.controller;

import com.jetbrains.filesystem.dto.JsonRpcRequest;
import com.jetbrains.filesystem.dto.JsonRpcResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/filemanage")
public class FileManageController {
    private static final Logger logger = LogManager.getLogger(FileManageController.class);

    @PostMapping
    public JsonRpcResponse handle(@RequestBody JsonRpcRequest request) {
        logger.info("Received JSON-RPC request: method={}, id={}", request.method, request.id);

        // Example: Implement one dummy method: "getServerTime"
        if ("getServerTime".equals(request.method)) {
            String serverTime = java.time.Instant.now().toString();
            return new JsonRpcResponse(serverTime, request.id);
        } else {
            // Return JSON-RPC error response
            String errorMsg = "Method not found: " + request.method;
            logger.warn(errorMsg);
            return new JsonRpcResponse(errorMsg, request.id, true);
        }
    }
}
