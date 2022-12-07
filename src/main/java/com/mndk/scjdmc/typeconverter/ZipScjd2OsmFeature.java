package com.mndk.scjdmc.typeconverter;

import com.mndk.scjdmc.util.ParsedOsmFeatureMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.util.file.ZipManager;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ZipScjd2OsmFeature extends ShpDirScjd2OsmFeature {

    private static final Logger LOGGER = LogManager.getLogger();

    public ZipScjd2OsmFeature(LayerFilterFunction layerFilter) {
        super(layerFilter);
    }

    @Override
    public ParsedOsmFeatureMap convert(File zipFile, String charset, ScjdParsedType parsedType) throws IOException {

        File zipDestination = new File(
                zipFile.getParent(),
                zipFile.getName().substring(0, zipFile.getName().lastIndexOf("."))
        );
        if(zipDestination.exists() && !zipDestination.delete()) {
            LOGGER.warn("Failed to delete directory: " + zipDestination);
        }

        ParsedOsmFeatureMap result;
        try {
            ZipManager.extractZipFile(zipFile, zipDestination, charset);
            result = super.convert(zipDestination, charset, parsedType);
        } catch(Throwable t) {
            throw (t instanceof IOException) ? (IOException) t : new IOException(t);
        } finally {
            DirectoryManager.deleteDirectory(zipDestination);
        }

        return result;
    }
}
