package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.dto.file.ReadFileSegmentParams;
import com.jetbrains.filesystem.service.LocalFileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadFileSegmentHandler implements JsonRpcMethodHandler<ReadFileSegmentParams> {
    private final LocalFileManager fileService;

    @Override
    public String method() {
        return "readFileSegment";
    }

    @Override
    public Object handle(ReadFileSegmentParams p) {
        return fileService.readFile(p.getPath(), p.getOffset(), p.getLength());
    }

    @Override
    public Class<ReadFileSegmentParams> paramType() {
        return ReadFileSegmentParams.class;
    }

}
