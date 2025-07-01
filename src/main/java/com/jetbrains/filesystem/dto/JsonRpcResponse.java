package com.jetbrains.filesystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    public String jsonrpc = "2.0";
    public Object result;
    public Object error;
    public Object id;

    public JsonRpcResponse(Object result, Object id) {
        this.result = result;
        this.id = id;
    }

    public JsonRpcResponse(Object error, Object id, boolean isError) {
        this.error = error;
        this.id = id;
    }
}