package com.jetbrains.filesystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {
    public String jsonrpc;
    public String method;
    public Object params;
    public Object id;
}
