package com.mndk.scjdmc.geojson.converter;

import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.util.file.ZipManager;
import org.geotools.geojson.feature.FeatureJSON;

import java.io.File;
import java.io.IOException;

public class ZipToGeoJsonConverter extends MultipleShpToGeoJsonConverter {

    public ZipToGeoJsonConverter(FeatureJSON featureJSON) {
        super(featureJSON);
    }

    @Override
    public void toFeatureJSON(File source, File destination, String charset) throws Exception {

        File zipDestination = new File(
                source.getParent(),
                source.getName().substring(0, source.getName().lastIndexOf("."))
        );

        try {
            if(zipDestination.exists() && !zipDestination.delete()) {
                LOGGER.warn("Failed to delete directory: " + zipDestination);
            }
            ZipManager.extractZipFile(source, zipDestination, charset);
            super.toFeatureJSON(zipDestination, destination, charset);
        } catch(Throwable t) {
            throw (t instanceof IOException) ? (IOException) t : new RuntimeException(t);
        }
        DirectoryManager.deleteDirectory(zipDestination);
    }
}
