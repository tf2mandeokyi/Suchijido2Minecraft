package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.column.LayerDataType;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

public class GeoJsonScjdReader {

    public static <T> T read(
            File jsonFile, Charset charset, ScjdDatasetReader.FCFunction<T> featureCollectionFunction
    ) throws IOException {

        Reader reader = new FileReader(jsonFile, charset);
        SimpleFeatureCollection featureCollection =
                (SimpleFeatureCollection) Constants.FEATURE_JSON.readFeatureCollection(reader);
        reader.close();

        LayerDataType layerDataType = LayerDataType.fromLayerName(jsonFile.getName());
        return featureCollectionFunction.apply(featureCollection, layerDataType);
    }

}
