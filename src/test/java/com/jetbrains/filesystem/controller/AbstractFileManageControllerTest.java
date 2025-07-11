package com.jetbrains.filesystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.service.LocalFileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractFileManageControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected FileServiceProperties properties;

    @Autowired
    protected LocalFileManager fileService;

    protected Path root;

    protected final String endpoint = "/api/v1/files";

    @BeforeEach
    void setupBase() {
        root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
    }

    @AfterEach
    void tearDownBase() throws IOException {
        Path testDir = root.resolve("test-folder");
        if (Files.exists(testDir)) {
            deleteRecursively(testDir);
        }
    }

    protected void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        if (Files.isDirectory(path)) {
            try (Stream<Path> paths = Files.list(path)) {
                for (Path child : paths.toList()) {
                    deleteRecursively(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }

}

