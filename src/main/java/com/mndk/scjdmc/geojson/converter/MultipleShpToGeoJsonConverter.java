package com.mndk.scjdmc.geojson.converter;

import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.scjd.MapIndexManager;
import org.geotools.geojson.feature.FeatureJSON;

import java.io.File;
import java.io.IOException;

public class MultipleShpToGeoJsonConverter extends ShpToGeoJsonConverter {

    public MultipleShpToGeoJsonConverter(FeatureJSON featureJSON) {
        super(featureJSON);
    }

    @Override
    public void toFeatureJSON(File directory, File destination, String charset) throws Exception {
        if(!directory.isDirectory()) {
            throw new IOException("Path should be directory");
        }

        File[] shapeFiles = directory.listFiles((dir, name) -> name.endsWith(".shp"));
        assert shapeFiles != null;

        if(shapeFiles.length == 0) {
            LOGGER.warn("No .shp file found in " + directory.getName());
        }

        String mapIndex = MapIndexManager.getMapIndexFromFileName(directory.getName());

        for(File shpFile : shapeFiles) {
            LayerDataType layerDataType = LayerDataType.fromLayerName(shpFile.getName());
            if(!this.layerFilter.apply(layerDataType)) continue;
            try {
                super.toFeatureJSON(mapIndex, shpFile, destination, charset);
            } catch(Throwable t) {
                LOGGER.error("Error thrown while parsing " + shpFile, t);
            }
        }
    }
}
