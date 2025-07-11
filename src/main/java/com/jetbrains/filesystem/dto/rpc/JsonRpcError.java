package com.jetbrains.filesystem.dto.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
public class JsonRpcError {
    private int code;
    private String message;
}
