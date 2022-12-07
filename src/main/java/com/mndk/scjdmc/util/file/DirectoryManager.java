package com.mndk.scjdmc.util.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class DirectoryManager {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void createParentFolders(File file, boolean throwIfFail) throws IOException {
		File parent = file.getParentFile();
		if(!parent.exists() && !parent.mkdirs()) {
			IOException exception = new IOException("Failed to create folder");

			if(throwIfFail) throw exception;
			else exception.printStackTrace();
		}
	}

	public static void createParentFolders(File file) throws IOException {
		createParentFolders(file, false);
	}

	public static File createFolder(File folder, String folderNameIfError) throws IOException {
		if(!folder.exists() && !folder.mkdir()) {
			if(folderNameIfError != null)
				throw new IOException("Failed to create " + folderNameIfError + " folder");
			else
				throw new IOException("Failed to create folder");
		}
		return folder;
	}

	/**
	 * Creates folder if not exists, and then returns it
	 * @param folder The folder
	 * @return The folder given as parameter
	 * @throws IOException
	 */
	public static File createFolder(File folder) throws IOException {
		return createFolder(folder, null);
	}

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
