package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.dto.file.MoveEntryParams;
import com.jetbrains.filesystem.service.LocalFileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoveEntryHandler implements JsonRpcMethodHandler<MoveEntryParams> {
    private final LocalFileManager fileService;

    @Override
    public String method() {
        return "moveEntry";
    }

    @Override
    public Object handle(MoveEntryParams p) {
        return fileService.moveEntry(p.getSourcePath(), p.getTargetPath());
    }

    @Override
    public Class<MoveEntryParams> paramType() {   // new helper
        return MoveEntryParams.class;
    }
}
