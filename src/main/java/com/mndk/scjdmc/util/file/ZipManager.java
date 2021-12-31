package com.mndk.scjdmc.util.file;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.net.URLDecoder;

public class ZipManager {

	public static void extractZipFile(File sourceZip, File destination, String charset) throws IOException {
		try(
				InputStream stream = new FileInputStream(URLDecoder.decode(sourceZip.getAbsolutePath(), "UTF-8"));
				ZipArchiveInputStream archive = new ZipArchiveInputStream(stream, charset, true, false, true)
		) {
			ZipArchiveEntry entry;
			while((entry = archive.getNextZipEntry()) != null) {
				File file = new File(destination.getPath(), entry.getName());
				if(entry.isDirectory()) {
					if(!file.isDirectory() && !file.mkdirs()) {
						throw new IOException("Failed to create directory: " + file);
					}
				} else {
					File parent = file.getParentFile();
					if(!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory: " + parent);
					}
					IOUtils.copy(archive, new FileOutputStream(file));
				}
			}
		}
	}

}
