package com.jetbrains.filesystem.registry;

import com.jetbrains.filesystem.handler.JsonRpcMethodHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class JsonRpcHandlerRegistry {
    private final Map<String, JsonRpcMethodHandler> handlerMap = new HashMap<>();

    public JsonRpcHandlerRegistry(List<JsonRpcMethodHandler> handlers) {
        for (JsonRpcMethodHandler handler : handlers) {
            handlerMap.put(handler.method(), handler);
            log.debug("Registered handler " + handler.getClass().getSimpleName());
        }
    }

    public JsonRpcMethodHandler getHandler(String method) {
        return handlerMap.get(method);
    }
}
