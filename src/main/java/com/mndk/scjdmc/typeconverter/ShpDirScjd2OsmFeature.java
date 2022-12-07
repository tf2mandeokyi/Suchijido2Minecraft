package com.mndk.scjdmc.typeconverter;

import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.ScjdMapIndexUtils;
import com.mndk.scjdmc.util.ParsedOsmFeatureMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;

import java.io.File;
import java.io.IOException;

public class ShpDirScjd2OsmFeature {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ShpScjd2OsmFeature featureConverter;


    public ShpDirScjd2OsmFeature(LayerFilterFunction layerFilter) {
        this.featureConverter = new ShpScjd2OsmFeature(layerFilter);
    }


    public ParsedOsmFeatureMap convert(
            File directory, String charset, ScjdParsedType parsedType
    ) throws IOException {
        if(!directory.isDirectory()) {
            throw new IOException("Path should be directory");
        }

        File[] shapeFiles = directory.listFiles((dir, name) -> name.endsWith(".shp"));
        assert shapeFiles != null;

        if(shapeFiles.length == 0) {
            LOGGER.warn("No .shp file found in " + directory.getName());
        }

        String mapName = parsedType == ScjdParsedType.INDEX ?
                ScjdMapIndexUtils.getMapIndexFromFileName(directory.getName()) :
                directory.getName();
        ParsedOsmFeatureMap result = new ParsedOsmFeatureMap(mapName, parsedType);

        for(File shpFile : shapeFiles) {
            try {
                LayerDataType layerDataType = LayerDataType.fromLayerName(shpFile.getName());
                SimpleFeatureCollection featureCollection = featureConverter.convert(mapName, shpFile, charset);
                if(featureCollection == null) continue;

                result.put(layerDataType, result.containsKey(layerDataType) ? combineCollections(
                        layerDataType, result.get(layerDataType), featureCollection
                ) : featureCollection);
            } catch(Throwable t) {
                LOGGER.error("Error thrown while parsing " + shpFile, t);
            }
        }

        return result;
    }


    private static SimpleFeatureCollection combineCollections(
            LayerDataType layerDataType, SimpleFeatureCollection a, SimpleFeatureCollection b
    ) {
        ListFeatureCollection result = new ListFeatureCollection(layerDataType.getNameFeatureType());
        SimpleFeatureIterator featureIterator = a.features();
        while(featureIterator.hasNext()) {
            result.add(featureIterator.next());
        }
        featureIterator = b.features();
        while(featureIterator.hasNext()) {
            result.add(featureIterator.next());
        }
        return result;
    }

}
