package com.mndk.scjdmc.util.file;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipException;

public class ZipManager {

	/**
	 * Extracts zip file to the destination
	 * @param sourceZip Zip file to be extracted
	 * @param destination Directory where the contents of the zip file are extracted to
	 * @param charset Charset of the zip file
	 * @return True if at least one file is extracted; false otherwise
	 */
	public static boolean extractZipFile(
			File sourceZip, File destination, Charset charset
	) throws IOException {

		boolean notEmpty = false;
		if(!destination.exists() && !destination.mkdir()) {
			throw new IOException("Failed to create destination directory: " + destination);
		}

		try(
				InputStream stream =
						Files.newInputStream(Paths.get(URLDecoder.decode(sourceZip.getAbsolutePath(), "UTF-8")));
				ZipArchiveInputStream archive =
						new ZipArchiveInputStream(stream, charset.name(), true, false, true)
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
					FileOutputStream os = new FileOutputStream(file);
					IOUtils.copy(archive, os);
					os.close();
					notEmpty = true;
				}
			}
		} catch(ZipException exception) {
			throw new IOException("Zip corruption found: " + sourceZip);
		}

		return notEmpty;
	}

}
