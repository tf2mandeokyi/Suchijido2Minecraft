package com.mndk.kvm2m.core.util.file;

import java.io.File;

public class DirectoryManager {

	public static void deleteDirectory(File file) {
		File[] files = file.listFiles();
		if(files == null) return;
		for(File subFile : files) {
			if(subFile.isDirectory()) deleteDirectory(subFile);
			else {
				if(!subFile.delete()) {
					throw new RuntimeException("Failed to delete the file");
				}
			}
		}
		if(!file.delete()) {
			throw new RuntimeException("Failed to delete the directory");
		}
	}

}
