package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeleteEntryHandler implements JsonRpcMethodHandler {
    private final FileManageService fileService;

    public DeleteEntryHandler(FileManageService fileService) {
        this.fileService = fileService;
    }

    @Override
    public String method() {
        return "deleteEntry";
    }

    @Override
    public Object handle(Map<String, Object> params) throws Exception {
        String path = (String) params.get("path");
        return fileService.deleteEntry(path);
    }
}
