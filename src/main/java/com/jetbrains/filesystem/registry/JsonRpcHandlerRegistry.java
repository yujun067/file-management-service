package com.jetbrains.filesystem.registry;

import com.jetbrains.filesystem.controller.FileManageController;
import com.jetbrains.filesystem.handler.JsonRpcMethodHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JsonRpcHandlerRegistry {
    private static final Logger logger = LogManager.getLogger(FileManageController.class);
    private final Map<String, JsonRpcMethodHandler> handlerMap = new HashMap<>();

    public JsonRpcHandlerRegistry(List<JsonRpcMethodHandler> handlers) {
        for (JsonRpcMethodHandler handler : handlers) {
            handlerMap.put(handler.method(), handler);
            logger.debug("Registered handler " + handler.getClass().getSimpleName());
        }
    }

    public JsonRpcMethodHandler getHandler(String method) {
        return handlerMap.get(method);
    }
}
