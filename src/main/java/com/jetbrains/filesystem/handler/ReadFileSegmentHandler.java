package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReadFileSegmentHandler implements JsonRpcMethodHandler {
    private final FileManageService fileService;

    public ReadFileSegmentHandler(FileManageService fileService) {
        this.fileService = fileService;
    }

    @Override
    public String method() {
        return "readFileSegment";
    }

    @Override
    public Object handle(Map<String, Object> params) throws Exception {
        String path = (String) params.get("path");
        Number offset = (Number) params.get("offset");
        Number length = (Number) params.get("length");
        return fileService.readFile(path, offset.longValue(), length.intValue());
    }
}
