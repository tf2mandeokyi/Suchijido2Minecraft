package com.mndk.kvm2m.core.util.file;

import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import sun.misc.Cleaner;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipManager {
	
	

	public static void extractZipFile(File sourceZip, File destination, String charset) throws IOException {

		FileChannel channel = FileChannel.open(sourceZip.toPath(), StandardOpenOption.READ);
		MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		InputStream stream = new ByteBufferBackedInputStream(mappedByteBuffer);
		DataInputStream dis = new DataInputStream(stream);
		if(dis.readInt() != 0x504B0708) { // Check if the file is "spanned ZIP archive"
			// Oops, the file wasn't "spanned ZIP archive"...
			// I should probably reinitialize the stream :)
			stream.close();
			mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			stream = new ByteBufferBackedInputStream(mappedByteBuffer);
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
		} finally {
			Cleaner cleaner = ((sun.nio.ch.DirectBuffer) mappedByteBuffer).cleaner();
			if(cleaner != null) cleaner.clean();
			stream.close();
			channel.close();
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
