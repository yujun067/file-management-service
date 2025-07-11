package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.dto.file.ListDirectoryParams;
import com.jetbrains.filesystem.service.LocalFileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListDirectoryHandler implements JsonRpcMethodHandler<ListDirectoryParams> {
    private final LocalFileManager fileService;

    @Override
    public String method() {
        return "listDirectoryChildren";
    }

    @Override
    public Object handle(ListDirectoryParams p)  {
        return fileService.listDirectoryChildren(p.getPath());
    }

    @Override
    public Class<ListDirectoryParams> paramType() {   // new helper
        return ListDirectoryParams.class;
    }
}
