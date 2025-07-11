package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.api.FileManager;
import com.jetbrains.filesystem.dto.file.CreateEntryParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateEntryHandler implements JsonRpcMethodHandler<CreateEntryParams> {
    private final FileManager fileService;

    @Override
    public String method() {
        return "createEntry";
    }

    @Override
    public Object handle(CreateEntryParams p)  {
        return fileService.createEntry(p.getPath(), p.getType());
    }

    @Override
    public Class<CreateEntryParams> paramType() {   // new helper
        return CreateEntryParams.class;
    }
}

