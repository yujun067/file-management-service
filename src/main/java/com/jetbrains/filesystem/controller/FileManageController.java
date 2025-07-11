package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.filesystem.exception.*;
import com.jetbrains.filesystem.dto.rpc.JsonRpcRequest;
import com.jetbrains.filesystem.dto.rpc.JsonRpcResponse;
import com.jetbrains.filesystem.registry.JsonRpcHandlerRegistry;
import com.jetbrains.filesystem.handler.JsonRpcMethodHandler;

import com.jetbrains.filesystem.util.JsonRpcErrorBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Log4j2
public class FileManageController {
    private final JsonRpcHandlerRegistry handlerRegistry;
    private final ObjectMapper objectMapper;

    @PostMapping()
    public Object handle(@RequestBody JsonNode payload, HttpServletRequest request) {
        if (payload.isArray()) {
            log.debug("batch request: {}", payload);
            List<JsonRpcResponse> responses = new ArrayList<>();

            for (JsonNode node : payload) {
                JsonRpcRequest singleRequest = null;
                try {
                    singleRequest = objectMapper.convertValue(node, JsonRpcRequest.class);
                    request.setAttribute("jsonrpc-id", singleRequest.getId());
                    JsonRpcResponse response = processSingle(singleRequest);
                    responses.add(response);
                } catch (FileServiceException fsEx) {
                    JsonRpcResponse errorResponse = JsonRpcErrorBuilder.fromFileServiceException(fsEx, request.getAttribute("jsonrpc-id"));
                    responses.add(errorResponse);
                    break; // stop processing remaining batch items
                } catch (Exception ex) {
                    JsonRpcResponse errorResponse = JsonRpcErrorBuilder.fromUnknownException(ex, request.getAttribute("jsonrpc-id"));
                    responses.add(errorResponse);
                    break; // stop processing remaining batch items
                }
            }

            return responses;
        } else {
            log.debug("single request: {}", payload);
            JsonRpcRequest singleRequest = null;
            try {
                singleRequest = objectMapper.convertValue(payload, JsonRpcRequest.class);
                request.setAttribute("jsonrpc-id", singleRequest.getId());
                return processSingle(singleRequest);
            } catch (FileServiceException fsEx) {
                return JsonRpcErrorBuilder.fromFileServiceException(fsEx, request.getAttribute("jsonrpc-id"));
            } catch (Exception ex) {
                return JsonRpcErrorBuilder.fromUnknownException(ex, request.getAttribute("jsonrpc-id"));
            }
        }
    }

    private JsonRpcResponse processSingle(JsonRpcRequest request) {
        log.debug("Received JSON-RPC request: method={}, id={}, params={}", request.getMethod(), request.getId(), request.getParams());

        JsonRpcMethodHandler handler = handlerRegistry.getHandler(request.getMethod());
        if (handler == null) {
            throw new MethodNotFoundException("method not found:" + request.getMethod());
        }

        Object typedParams = objectMapper.convertValue(request.getParams(), handler.paramType());
        Object result = handler.handle(typedParams);
        return new JsonRpcResponse(result, request.getId());
    }

}
