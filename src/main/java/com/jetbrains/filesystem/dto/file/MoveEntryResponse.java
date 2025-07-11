package com.jetbrains.filesystem.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveEntryResponse {
    private String sourcePath;
    private String targetPath;
}
