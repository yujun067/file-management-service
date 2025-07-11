package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.api.FileManager;
import com.jetbrains.filesystem.dto.file.DeleteEntryParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteEntryHandler implements JsonRpcMethodHandler<DeleteEntryParams> {
    private final FileManager fileService;

    @Override
    public String method() {
        return "deleteEntry";
    }

    @Override
    public Object handle(DeleteEntryParams p)  {
        return fileService.deleteEntry(p.getPath());
    }

    @Override
    public Class<DeleteEntryParams> paramType() {   // new helper
        return DeleteEntryParams.class;
    }
}
