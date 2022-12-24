package com.mndk.scjdmc.typeconverter;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.file.ScjdFileInformation;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.function.FeatureFilter;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Scjd2OsmFeatureConverter {


    public static ScjdDirectoryParsedMap<SimpleFeatureCollection> parseShpAsOsmFeature(
            File file, Charset charset, ScjdParsedType parsedType
    ) throws IOException {
        return parseShpAsOsmFeature(file, charset, parsedType, t -> true, f -> true);
    }


    public static ScjdDirectoryParsedMap<SimpleFeatureCollection> parseShpAsOsmFeature(
            File file, Charset charset, ScjdParsedType parsedType, LayerFilterFunction layerFilter,
            FeatureFilter featureFilter
    ) throws IOException {
        ScjdDatasetReader reader = ScjdDatasetReader.getShpReader(file);
        reader.setLayerFilter(layerFilter);

        return parseAsOsmFeature(file, charset, parsedType, reader, featureFilter);
    }


    public static ScjdDirectoryParsedMap<SimpleFeatureCollection> parseAsOsmFeature(
            File file, Charset charset, ScjdParsedType parsedType, ScjdDatasetReader reader,
            FeatureFilter featureFilter
    ) throws IOException {
        ScjdFileInformation fileInformation = new ScjdFileInformation(file, parsedType);

        ScjdDirectoryParsedMap<SimpleFeatureCollection> result = reader.read(
                file, charset, parsedType, (sfc, layerDataType) ->
                        toOsmStyleFeatureCollection(
                                sfc, layerDataType, featureFilter,
                                i -> fileInformation.getNameOrIndex() + "-" + layerDataType.getLayerName() + "-" + i
                        )
        );

        Geometry coastlineGeometry = result.getCoastlineGeometry();
        if(coastlineGeometry != null) {
            SimpleFeatureType coastlineOsmFeatureType = LayerDataType.해안선.getOsmFeatureType();

            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(coastlineOsmFeatureType);
            featureBuilder.set(Constants.GEOMETRY_PROPERTY_NAME, coastlineGeometry);

            SimpleFeature coastline = featureBuilder.buildFeature("coastline");
            coastline = LayerDataType.해안선.toOsmStyleFeature(coastline, "coastline");

            SimpleFeatureCollection coastlineFeatureCollection = new ListFeatureCollection(
                    coastlineOsmFeatureType, Collections.singletonList(coastline)
            );
            result.put(LayerDataType.해안선, coastlineFeatureCollection);
        }

        return result;
    }


    static SimpleFeatureCollection toOsmStyleFeatureCollection(
            SimpleFeatureCollection featureCollection, LayerDataType layerDataType,
            FeatureFilter featureFilter, Function<Integer, String> idGenerator
    ) {
        SimpleFeatureType featureType = layerDataType.getOsmFeatureType();

        if(featureType == null || featureCollection == null) return null;

        int i = 0;
        List<SimpleFeature> features = new ArrayList<>();
        try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
            while(featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                if(!featureFilter.apply(feature)) continue;

                features.add(
                        layerDataType.toOsmStyleFeature(feature, idGenerator.apply(++i))
                );
            }
        }

        return new ListFeatureCollection(featureType, features);
    }
}
