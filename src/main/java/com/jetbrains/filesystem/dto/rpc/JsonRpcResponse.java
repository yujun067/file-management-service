package com.jetbrains.filesystem.dto.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class JsonRpcResponse {
    private String jsonrpc = "2.0";
    private Object result;
    private JsonRpcError error;
    private JsonNode id;

    public JsonRpcResponse(Object result, JsonNode id) {
        this.result = result;
        this.id = id;
    }

    public JsonRpcResponse(JsonRpcError error, JsonNode id, boolean isError) {
        this.error = error;
        this.id = id;
    }

}