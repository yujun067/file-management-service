package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.api.FileManager;
import com.jetbrains.filesystem.dto.file.AppendDataToFileParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class AppendDataToFileHandler implements JsonRpcMethodHandler<AppendDataToFileParams> {
    private final FileManager fileService;

    @Override
    public String method() {
        return "appendDataToFile";
    }

    @Override
    public Object handle(AppendDataToFileParams p)  {
        log.debug("appendDataToFile:{}", p.toString());
        return fileService.appendDataToFile(p.getPath(),p.getData());
    }

    @Override
    public Class<AppendDataToFileParams> paramType() {   // new helper
        return AppendDataToFileParams.class;
    }
}
