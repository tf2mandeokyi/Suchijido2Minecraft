package com.mndk.kvm2m.core.util.file;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipManager {
	
	

	public static void extractZipFile(File sourceZip, File destination, String charset) throws IOException {

		/*
		try(InputStream inputStream = new FileInputStream(sourceZip);
			ZipArchiveInputStream zipArchiveInputStream =
					new ZipArchiveInputStream(inputStream, charset, false, true)) {

			ArchiveEntry entry;
			while ((entry = zipArchiveInputStream.getNextEntry()) != null) {
				String entryFileName = entry.getName();
				File entryFile = new File(destination, entryFileName);
				byte[] buffer = new byte[1024];
				try(OutputStream outputStream = new FileOutputStream(entryFile)) {
					int length;
					while((length = zipArchiveInputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, length);
					}
					outputStream.flush();
				}
			}
		}
		*/

		InputStream stream = new FileInputStream(sourceZip);
		DataInputStream dis = new DataInputStream(stream);
		if(dis.readInt() != 0x504B0708) { // The sussy zip
			stream.close();
			stream = new FileInputStream(sourceZip);
		}

		try (ZipInputStream zis = new ZipInputStream(stream, Charset.forName(charset))) {
			ZipEntry zipEntry = zis.getNextEntry();
			byte[] buffer = new byte[1024];
			while(zipEntry != null) {
				File newFile = ZipManager.newFile(destination, zipEntry);
			     if (zipEntry.isDirectory()) {
			         if (!newFile.isDirectory() && !newFile.mkdirs()) {
			             throw new IOException("Failed to create directory " + newFile);
			         }
			     } else {
			         // fix for Windows-created archives
			         File parent = newFile.getParentFile();
			         if (!parent.isDirectory() && !parent.mkdirs()) {
			             throw new IOException("Failed to create directory " + parent);
			         }
			         
			         // write file content
			         FileOutputStream fos = new FileOutputStream(newFile);
			         int len;
			         while ((len = zis.read(buffer)) > 0) {
			             fos.write(buffer, 0, len);
			         }
			         fos.close();
			     }
			     zipEntry = zis.getNextEntry();
			}
		}
	}

	
	
	private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
	    File destFile = new File(destinationDir, zipEntry.getName());
	
	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();
	
	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }
	
	    return destFile;
	}
	
	

}
