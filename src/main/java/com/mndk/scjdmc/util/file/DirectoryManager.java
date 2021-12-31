package com.mndk.scjdmc.util.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class DirectoryManager {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void deleteDirectory(File file) {
		File[] files = file.listFiles();
		if(files == null) return;
		for(File subFile : files) {
			if(subFile.isDirectory()) deleteDirectory(subFile);
			else {
				if(!subFile.delete()) {
					LOGGER.error("Failed to delete file: " + subFile);
				}
			}
		}
		if(!file.delete()) {
			LOGGER.error("Failed to delete directory: " + file.getAbsolutePath());
		}
	}

}
