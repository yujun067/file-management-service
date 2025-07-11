package com.jetbrains.filesystem.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadFileSegmentParams {
    private String path;
    private long offset;
    private int length;
}
