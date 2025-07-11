package com.jetbrains.filesystem.dto.rpc;

public enum JsonRpcErrorCode {
    METHOD_NOT_FOUND(-32601, "Method not found"),
    INVALID_PARAMS(-32602, "Invalid params"),
    INTERNAL_ERROR(-32603, "Internal server error"),
    FILE_NOT_FOUND(-32000, "File not found"),
    FILE_OPP_ERROR(-32001, "File Operation Error"),
    CONFLICT_ERROR(-32002, "Conflict Error");

    private final int code;
    private final String defaultMessage;

    JsonRpcErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
