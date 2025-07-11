package com.jetbrains.filesystem.dto.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonRpcRequest {
    private String jsonrpc;
    private String method;
    private JsonNode params;
    private JsonNode id;
}
