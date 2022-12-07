package com.mndk.scjdmc.geojson.converter;

import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.ScjdMapIndexUtils;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;

import java.io.File;
import java.io.IOException;

@Deprecated
public class Scjd2OsmStyleFeatureMapConverter extends ScjdShapefileConverter<ShapefileConversionResult> {

    private final Scjd2OsmStyleFeatureConverter converter;

    public Scjd2OsmStyleFeatureMapConverter() {
        super();
        this.converter = new Scjd2OsmStyleFeatureConverter();
    }

    @Override
    public ShapefileConversionResult convert(File directory, String charset) throws Exception {
        if(!directory.isDirectory()) {
            throw new IOException("Path should be directory");
        }

        File[] shapeFiles = directory.listFiles((dir, name) -> name.endsWith(".shp"));
        assert shapeFiles != null;

        if(shapeFiles.length == 0) {
            LOGGER.warn("No .shp file found in " + directory.getName());
        }

        String mapIndex = ScjdMapIndexUtils.getMapIndexFromFileName(directory.getName());
        ShapefileConversionResult result = new ShapefileConversionResult(mapIndex);

        for(File shpFile : shapeFiles) {
            LayerDataType layerDataType = LayerDataType.fromLayerName(shpFile.getName());
            if(!this.layerFilter.apply(layerDataType)) continue;
            try {
                SimpleFeatureCollection featureCollection = converter.convert(mapIndex, shpFile, charset);
                if(featureCollection != null) {
                    if (result.containsKey(layerDataType)) {
                        ListFeatureCollection newFeatureCollection = new ListFeatureCollection(layerDataType.getNameFeatureType());
                        SimpleFeatureIterator featureIterator = result.get(layerDataType).features();
                        while(featureIterator.hasNext()) {
                            newFeatureCollection.add(featureIterator.next());
                        }
                        featureIterator = featureCollection.features();
                        while(featureIterator.hasNext()) {
                            newFeatureCollection.add(featureIterator.next());
                        }
                        result.put(layerDataType, newFeatureCollection);
                    } else {
                        result.put(layerDataType, featureCollection);
                    }
                }
            } catch(Throwable t) {
                LOGGER.error("Error thrown while parsing " + shpFile, t);
            }
        }

        return result;
    }
}
