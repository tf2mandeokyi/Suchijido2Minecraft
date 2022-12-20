package com.mndk.scjdmc.typeconverter;

import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdFileInformation;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import com.mndk.scjdmc.util.function.ScjdFeatureCollectionFunction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Scjd2OsmFeature {


    public static ScjdDirectoryParsedMap<SimpleFeatureCollection> parseAsOsmFeature(
            File file, Charset charset, ScjdParsedType parsedType, LayerFilterFunction layerFilter
    ) throws IOException {
        ScjdFileInformation fileInformation = new ScjdFileInformation(file, parsedType);
        ScjdFeatureCollectionFunction<SimpleFeatureCollection> featureCollectionFunction = (sfc, layerDataType) ->
                toOsmStyleFeatureCollection(
                        sfc, layerDataType,
                        i -> fileInformation.getNameOrIndex() + "-" + layerDataType.getLayerName() + "-" + i
                );

        ScjdDatasetReader reader = ScjdDatasetReader.getShpReader(file);
        return reader.read(file, charset, parsedType, featureCollectionFunction);
    }


    public static ScjdDirectoryParsedMap<SimpleFeatureCollection> parseAsOsmFeature(
            File file, Charset charset, ScjdParsedType parsedType
    ) throws IOException {
        return parseAsOsmFeature(file, charset, parsedType, f -> true);
    }


    static SimpleFeatureCollection toOsmStyleFeatureCollection(
            SimpleFeatureCollection featureCollection, LayerDataType layerDataType,
            Function<Integer, String> idGenerator
    ) {
        SimpleFeatureType featureType = layerDataType.getOsmFeatureType();

        if(featureType == null || featureCollection == null) return null;

        int i = 0;
        List<SimpleFeature> features = new ArrayList<>();
        try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
            while(featureIterator.hasNext()) {
                features.add(
                        layerDataType.toOsmStyleFeature(featureIterator.next(), idGenerator.apply(++i))
                );
            }
        }

        return new ListFeatureCollection(featureType, features);
    }
}
