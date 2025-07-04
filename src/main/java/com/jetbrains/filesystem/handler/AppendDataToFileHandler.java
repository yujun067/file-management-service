package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppendDataToFileHandler implements JsonRpcMethodHandler {
    private final FileManageService fileService;

    public AppendDataToFileHandler(FileManageService fileService) {
        this.fileService = fileService;
    }

    @Override
    public String method() {
        return "appendDataToFile";
    }

    @Override
    public Object handle(Map<String, Object> params) throws Exception {
        String path = (String) params.get("path");
        String data = (String) params.get("data");
        return fileService.appendDataToFile(path, data);
    }
}
