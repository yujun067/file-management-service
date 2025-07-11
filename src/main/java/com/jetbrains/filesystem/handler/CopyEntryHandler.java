package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.api.FileManager;
import com.jetbrains.filesystem.dto.file.CopyEntryParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CopyEntryHandler implements JsonRpcMethodHandler<CopyEntryParams> {
    private final FileManager fileService;

    @Override
    public String method() {
        return "copyEntry";
    }

    @Override
    public Object handle(CopyEntryParams p)  {
        return fileService.copyEntry(p.getSourcePath(), p.getTargetPath());
    }

    @Override
    public Class<CopyEntryParams> paramType() {   // new helper
        return CopyEntryParams.class;
    }
}
