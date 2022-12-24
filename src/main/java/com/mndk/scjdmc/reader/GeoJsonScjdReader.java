package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.function.ScjdFeatureCollectionFunction;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class GeoJsonScjdReader {

    public static <T> T read(
            File jsonFile, ScjdFeatureCollectionFunction<T> featureCollectionFunction
    ) throws IOException {

        InputStream stream = Files.newInputStream(jsonFile.toPath());
        SimpleFeatureCollection featureCollection =
                (SimpleFeatureCollection) Constants.FEATURE_JSON.readFeatureCollection(stream);
        stream.close();

        LayerDataType layerDataType = LayerDataType.fromLayerName(jsonFile.getName());
        return featureCollectionFunction.apply(featureCollection, layerDataType);
    }

}
