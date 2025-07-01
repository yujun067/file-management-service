package com.jetbrains.filesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class FilesystemApplication {
	private static final Logger logger = LogManager.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FilesystemApplication.class, args);
		logger.debug("This is a DEBUG log");
		logger.info("This is an INFO log");
		logger.warn("This is a WARN log");
		logger.error("This is an ERROR log");
	}

}
