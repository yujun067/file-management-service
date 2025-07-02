package com.jetbrains.filesystem.controller;

import com.jetbrains.filesystem.dto.*;
import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/filemanage")
public class FileManageController {
    private static final Logger logger = LogManager.getLogger(FileManageController.class);
    private final FileManageService fileService;

    public FileManageController(FileManageService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public JsonRpcResponse handle(@RequestBody JsonRpcRequest request)  {
        logger.info("Received JSON-RPC request: method={}, id={}", request.getMethod(), request.getId());
        JsonRpcResponse response = null;
        Map<String, Object> params = (Map<String, Object>) request.getParams();

        try {
            if ("getFileInfo".equals(request.getMethod())) {
                String path = (String) params.get("path");
                GetFileInfoResponse result = fileService.getFileInfo(path);
                response = new JsonRpcResponse(result, request.getId());
            } else if("listDirectoryChildren".equals(request.getMethod())) {
                String path = (String) params.get("path");
                List<FileInfo> result = fileService.listDirectoryChildren(path);
                response = new JsonRpcResponse(result, request.getId());
            } else if("createEntry".equals(request.getMethod())) {
                String path = (String) params.get("path");
                //2 types: file, folder
                String type = (String) params.get("type");
                CreateEntryResponse result = fileService.createEntry(path, type);
                response = new JsonRpcResponse(result, request.getId());
            } else if("deleteEntry".equals(request.getMethod())) {
                String path = (String) params.get("path");
                DeleteEntryResponse result = fileService.deleteEntry(path);
                response = new JsonRpcResponse(result, request.getId());
            } else if("moveEntry".equals(request.getMethod())) {
                String sourcePath = (String) params.get("sourcePath");
                String targetPath = (String) params.get("targetPath");
                MoveEntryResponse result = fileService.moveEntry(sourcePath, targetPath);
                response = new JsonRpcResponse(result, request.getId());
            } else if("copyEntry".equals(request.getMethod())) {
                String sourcePath = (String) params.get("sourcePath");
                String targetPath = (String) params.get("targetPath");
                CopyEntryResponse result = fileService.copyEntry(sourcePath, targetPath);
                response = new JsonRpcResponse(result, request.getId());
            } else {
                return buildErrorResponse(request.getId(), -32601, "Method not found: " + request.getMethod());
            }
        } catch(FileNotFoundException e) {
            return buildErrorResponse(request.getId(), -32000, "File not found: " + request.getMethod());
        } catch(IOException e) {
            return buildErrorResponse(request.getId(), -32001, "IO Error: " + e.getMessage());
        } catch(IllegalArgumentException e) {
            return buildErrorResponse(request.getId(), -32602, "Illegal Argument:" + e.getMessage());
        } catch(Exception e) {
            return buildErrorResponse(request.getId(), -32603, "Internal server error: " + e.getMessage());
        }

        return response;
    }


    private JsonRpcResponse buildErrorResponse(Object id, int code, String message) {
        logger.warn(message);
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        JsonRpcResponse response = new JsonRpcResponse(error, id, true);
        return response;
    }


}
