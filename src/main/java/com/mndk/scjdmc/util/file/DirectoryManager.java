package com.mndk.scjdmc.util.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DirectoryManager {

	private static final Logger LOGGER = LogManager.getLogger();


	public static void createParentFolders(File file, boolean throwIfFail) throws IOException {
		File parent = file.getParentFile();
		if(!parent.exists() && !parent.mkdirs()) {
			IOException exception = new IOException("Failed to create folder");

			if(throwIfFail) throw exception;
			else LOGGER.error(exception);
		}
	}


	public static void createParentFolders(File file) throws IOException {
		createParentFolders(file, false);
	}


	public static File createFolder(File folder, String folderNameIfError) throws IOException {
		if(!folder.exists() && !folder.mkdirs()) {
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
     */
	public static File createFolder(File folder) throws IOException {
		return createFolder(folder, null);
	}


	public static void moveDirectoryContents(File sourceDir, File destinationDir) throws IOException {
		if(!sourceDir.isDirectory()) return;

		File[] sourceContents = sourceDir.listFiles();
		assert sourceContents != null;

		for(File sourceContent : sourceContents) {
			String fileName = sourceContent.getName();
			File newContentPath = new File(destinationDir, fileName);
			Files.move(sourceContent.toPath(), newContentPath.toPath());
		}
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
