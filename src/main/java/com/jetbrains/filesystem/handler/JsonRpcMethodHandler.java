package com.jetbrains.filesystem.handler;

public interface JsonRpcMethodHandler<P> {
    String method();
    Class<P> paramType();
    Object handle(P params);
}
