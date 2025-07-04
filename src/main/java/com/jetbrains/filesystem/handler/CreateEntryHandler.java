package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateEntryHandler implements JsonRpcMethodHandler {
    private final FileManageService fileService;

    public CreateEntryHandler(FileManageService fileService) {
        this.fileService = fileService;
    }

    @Override
    public String method() {
        return "createEntry";
    }

    @Override
    public Object handle(Map<String, Object> params) throws Exception {
        String path = (String) params.get("path");
        String type = (String) params.get("type");
        return fileService.createEntry(path, type);
    }
}

