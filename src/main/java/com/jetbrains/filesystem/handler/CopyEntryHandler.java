package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CopyEntryHandler implements JsonRpcMethodHandler {
    private final FileManageService fileService;

    public CopyEntryHandler(FileManageService fileService) {
        this.fileService = fileService;
    }

    @Override
    public String method() {
        return "copyEntry";
    }

    @Override
    public Object handle(Map<String, Object> params) throws Exception {
        String source = (String) params.get("sourcePath");
        String target = (String) params.get("targetPath");
        return fileService.copyEntry(source, target);
    }
}
