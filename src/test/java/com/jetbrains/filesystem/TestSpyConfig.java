package com.jetbrains.filesystem;

import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.service.FileManageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.spy;

@TestConfiguration
public class TestSpyConfig {

    @Bean
    public FileManageService fileManageService(FileServiceProperties properties) {
        return spy(new FileManageService(properties));
    }
}
