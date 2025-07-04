package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.io.IOException;

@Component
public class GetFileInfoHandler implements JsonRpcMethodHandler {
    private final FileManageService fileService;

    public GetFileInfoHandler(FileManageService fileService) {
        this.fileService = fileService;
    }

    @Override
    public String method() {
        return "getFileInfo";
    }

    @Override
    public Object handle(Map<String, Object> params) throws Exception {
        String path = (String) params.get("path");
        return fileService.getFileInfo(path);
    }
}
