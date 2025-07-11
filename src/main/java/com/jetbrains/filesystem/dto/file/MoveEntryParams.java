package com.jetbrains.filesystem.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveEntryParams {
    private String sourcePath;
    private String targetPath;
}
