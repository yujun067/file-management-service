package com.jetbrains.filesystem.handler;

import java.util.Map;

public interface JsonRpcMethodHandler {
    String method(); // e.g. "getFileInfo"
    Object handle(Map<String, Object> params) throws Exception;
}
