package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MoveEntryHandler implements JsonRpcMethodHandler {
    private final FileManageService fileService;

    public MoveEntryHandler(FileManageService fileService) {
        this.fileService = fileService;
    }

    @Override
    public String method() {
        return "moveEntry";
    }

    @Override
    public Object handle(Map<String, Object> params) throws Exception {
        String source = (String) params.get("sourcePath");
        String target = (String) params.get("targetPath");
        return fileService.moveEntry(source, target);
    }
}
