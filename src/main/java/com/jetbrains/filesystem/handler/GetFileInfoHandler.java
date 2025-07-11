package com.jetbrains.filesystem.handler;

import com.jetbrains.filesystem.dto.file.GetFileInfoParams;
import com.jetbrains.filesystem.service.LocalFileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class GetFileInfoHandler implements JsonRpcMethodHandler<GetFileInfoParams> {
    private final LocalFileManager fileService;

    @Override
    public String method() {
        return "getFileInfo";
    }

    @Override
    public Object handle(GetFileInfoParams p)  {
        log.debug("getFileInfo:{}", p.toString());
        return fileService.getFileInfo(p.getPath());
    }

    @Override
    public Class<GetFileInfoParams> paramType() {
        return GetFileInfoParams.class;
    }
}
