package com.jetbrains.filesystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fileservice")
public class FileServiceProperties {
    private String rootFolder;

    // getter and setter
    public String getRootFolder() {
        return rootFolder;
    }
}

