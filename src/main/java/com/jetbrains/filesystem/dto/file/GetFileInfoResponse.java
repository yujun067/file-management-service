package com.jetbrains.filesystem.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetFileInfoResponse {
    private String name;
    private String path;
    private long size;
    private boolean directory;
}
