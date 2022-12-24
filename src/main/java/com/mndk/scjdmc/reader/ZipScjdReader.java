package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.util.file.ZipManager;
import com.mndk.scjdmc.util.function.ScjdFeatureCollectionFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ZipScjdReader extends ShpDirScjdReader {


    private static final Logger LOGGER = LogManager.getLogger();


    @Override
    public <T> ScjdDirectoryParsedMap<T> read(
            File zipFile, Charset charset, ScjdParsedType parsedType,
            ScjdFeatureCollectionFunction<T> featureCollectionFunction
    ) throws IOException {

        File zipDestination = new File(
                zipFile.getParent(),
                zipFile.getName().substring(0, zipFile.getName().lastIndexOf("."))
        );
        if(zipDestination.exists() && !zipDestination.delete()) {
            LOGGER.warn("Failed to delete directory: " + zipDestination);
        }

        try {
            boolean notEmpty = ZipManager.extractZipFile(zipFile, zipDestination, charset);
            if(!notEmpty) LOGGER.warn("Zip file is empty: " + zipFile);

            correctZipContentToStandard(zipDestination, charset);

            return super.read(zipDestination, charset, parsedType, featureCollectionFunction);
        } catch(Throwable t) {
            throw (t instanceof IOException iot) ? iot : new IOException(t);
        } finally {
            DirectoryManager.deleteDirectory(zipDestination);
        }
    }


    private static void correctZipContentToStandard(File extractedZipDirectory, Charset charset) throws IOException {
        File[] innerZipFiles = extractedZipDirectory.listFiles(f -> f.getName().endsWith(".zip"));
        if(innerZipFiles != null) {
            for (File innerZipFile : innerZipFiles) {
                ZipManager.extractZipFile(innerZipFile, extractedZipDirectory, charset);
            }
        }

        File[] innerDirectories = extractedZipDirectory.listFiles(File::isDirectory);
        if(innerDirectories != null) {
            for (File innerDirectory : innerDirectories) {
                DirectoryManager.moveDirectoryContents(innerDirectory, extractedZipDirectory);
            }
        }
    }

}
