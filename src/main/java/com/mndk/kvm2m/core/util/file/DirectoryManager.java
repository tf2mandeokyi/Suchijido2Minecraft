package com.mndk.kvm2m.core.util.file;

import java.io.File;

public class DirectoryManager {

	public static void deleteDirectory(File file) {
		for(File subFile : file.listFiles()) {
			if(subFile.isDirectory()) deleteDirectory(subFile);
			else subFile.delete();
		}
		file.delete();
	}

}
