package com.jetbrains.filesystem.controller;

import com.jetbrains.filesystem.dto.*;
import com.jetbrains.filesystem.registry.JsonRpcHandlerRegistry;
import com.jetbrains.filesystem.handler.JsonRpcMethodHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/filemanage")
public class FileManageController {
    private static final Logger logger = LogManager.getLogger(FileManageController.class);

    private final JsonRpcHandlerRegistry handlerRegistry;

    public FileManageController(JsonRpcHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    @PostMapping
    public JsonRpcResponse handle(@RequestBody JsonRpcRequest request) {
        logger.info("Received JSON-RPC request: method={}, id={}", request.getMethod(), request.getId());

        try {
            JsonRpcMethodHandler handler = handlerRegistry.getHandler(request.getMethod());
            if (handler == null) {
                return buildErrorResponse(request.getId(), JsonRpcErrorCode.METHOD_NOT_FOUND, request.getMethod());
            }

            Map<String, Object> params = castParams(request.getParams());
            Object result = handler.handle(params);
            return new JsonRpcResponse(result, request.getId());
        } catch (FileNotFoundException e) {
            return buildErrorResponse(request.getId(), JsonRpcErrorCode.FILE_NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            return buildErrorResponse(request.getId(), JsonRpcErrorCode.IO_ERROR, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(request.getId(), JsonRpcErrorCode.INVALID_PARAMS, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(request.getId(), JsonRpcErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    private Map<String, Object> castParams(Object rawParams) {
        if (rawParams instanceof Map<?, ?>) {
            return (Map<String, Object>) rawParams;
        }
        throw new IllegalArgumentException("Parameters must be a JSON object");
    }

    private JsonRpcResponse buildErrorResponse(Object id, JsonRpcErrorCode code, String details) {
        String message = code.getDefaultMessage() + (details != null ? ": " + details : "");
        logger.warn("Error ({}): {}", code.getCode(), message);
        Map<String, Object> error = Map.of(
                "code", code.getCode(),
                "message", message
        );
        return new JsonRpcResponse(error, id, true);
    }
}
