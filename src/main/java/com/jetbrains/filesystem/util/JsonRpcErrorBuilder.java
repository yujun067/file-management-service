package com.jetbrains.filesystem.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.jetbrains.filesystem.dto.rpc.JsonRpcError;
import com.jetbrains.filesystem.dto.rpc.JsonRpcErrorCode;
import com.jetbrains.filesystem.dto.rpc.JsonRpcResponse;
import com.jetbrains.filesystem.exception.*;

public class JsonRpcErrorBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonRpcResponse fromFileServiceException(FileServiceException ex, Object id) {
        JsonNode idNode = convertId(id);
        JsonRpcErrorCode code = mapToErrorCode(ex);
        return buildErrorResponse(idNode, code, ex.getMessage());
    }

    public static JsonRpcResponse fromUnknownException(Exception ex, Object id) {
        JsonNode idNode = convertId(id);
        return buildErrorResponse(idNode, JsonRpcErrorCode.INTERNAL_ERROR, ex.getMessage());
    }

    private static JsonNode convertId(Object id) {
        if (id == null) {
            return NullNode.getInstance();
        }
        return objectMapper.valueToTree(id);
    }

    private static JsonRpcResponse buildErrorResponse(JsonNode id, JsonRpcErrorCode code, String detail) {
        String message = code.getDefaultMessage();
        if (detail != null && !detail.isBlank()) {
            message += ": " + detail;
        }
        JsonRpcError error = new JsonRpcError(code.getCode(), message);
        return new JsonRpcResponse(error, id, true);
    }

    private static JsonRpcErrorCode mapToErrorCode(FileServiceException ex) {
        if (ex instanceof NotFoundException) {
            return JsonRpcErrorCode.FILE_NOT_FOUND;
        } else if (ex instanceof ValidationException) {
            return JsonRpcErrorCode.INVALID_PARAMS;
        } else if (ex instanceof ConflictException) {
            return JsonRpcErrorCode.CONFLICT_ERROR;
        } else if (ex instanceof FileOperationException) {
            return JsonRpcErrorCode.FILE_OPP_ERROR;
        } else if (ex instanceof MethodNotFoundException) {
            return JsonRpcErrorCode.METHOD_NOT_FOUND;
        } else {
            return JsonRpcErrorCode.INTERNAL_ERROR;
        }
    }
}
