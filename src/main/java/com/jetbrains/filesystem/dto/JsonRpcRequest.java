package com.jetbrains.filesystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {
    public String jsonrpc;
    public String method;
    public Object params;
    public Object id;
}
