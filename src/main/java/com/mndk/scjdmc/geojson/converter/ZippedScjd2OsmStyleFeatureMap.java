package com.mndk.scjdmc.geojson.converter;

import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.util.file.ZipManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

@Deprecated
public class ZippedScjd2OsmStyleFeatureMap extends Scjd2OsmStyleFeatureMapConverter {

    public ZippedScjd2OsmStyleFeatureMap() {
        super();
    }

    @Override
    public ShapefileConversionResult convert(File source, Charset charset) throws Exception {

        File zipDestination = new File(
                source.getParent(),
                source.getName().substring(0, source.getName().lastIndexOf("."))
        );

        ShapefileConversionResult result;

        try {
            if(zipDestination.exists() && !zipDestination.delete()) {
                LOGGER.warn("Failed to delete directory: " + zipDestination);
            }
            ZipManager.extractZipFile(source, zipDestination, charset);
            result = super.convert(zipDestination, charset);
        } catch(Throwable t) {
            throw (t instanceof IOException) ? (IOException) t : new RuntimeException(t);
        }
        DirectoryManager.deleteDirectory(zipDestination);

        return result;
    }
}
