package com.jetbrains.filesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class FilesystemApplication {
	private static final Logger logger = LogManager.getLogger(FilesystemApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FilesystemApplication.class, args);
		logger.info("Application started with Log4j2!");
	}

}
