package com.mndk.scjdmc.relocator;

import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.util.*;
import com.mndk.scjdmc.util.file.ScjdFileInformation;
import com.mndk.scjdmc.util.io.SimpleFeatureJsonWriter;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Scjd2TppDatasetRelocator {


    public static void relocate(File sourceFile, Charset sourceEncoding,
                                ScjdParsedType parsedType, ScjdDatasetReader reader,
                                File tppDatasetFolder, Charset destinationEncoding,
                                double buffer, boolean cutFeatures) throws IOException {

        Map<Pair<TppTileCoordinate, LayerDataType>, Integer> coordinateTypeCountMap = new HashMap<>();
        Map<LayerDataType, Integer> typeCountMap = new HashMap<>();
        ScjdFileInformation fileInformation = new ScjdFileInformation(sourceFile, parsedType);

        reader.read(sourceFile, sourceEncoding, parsedType, (featureCollection, layerDataType) -> {
            Map<TppTileCoordinate, SimpleFeatureJsonWriter> writerMap = new HashMap<>();

            SimpleFeatureIterator featureIterator = featureCollection.features();

            int typeCount = IntegerMapUtils.increment(typeCountMap, layerDataType, 1);
            ProgressBar progressBar = ProgressBarUtils.createProgressBar(
                    String.format("Relocating: %s - %s#%d",
                            fileInformation.getNameForFile(), layerDataType.getEnglishName(), typeCount
                    ), featureCollection.size()
            );

            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();

                BoundingBox featureBoundingBox = feature.getBounds();
                Set<TppTileCoordinate> tileCoordinates =
                        TppTileCoordinate.getBoundingBoxIntersections(featureBoundingBox, buffer);

                for (TppTileCoordinate coordinate : tileCoordinates) {
                    Pair<TppTileCoordinate, LayerDataType> pair = Pair.of(coordinate, layerDataType);
                    SimpleFeatureJsonWriter writer = writerMap.computeIfAbsent(coordinate, c -> {
                        int coordinateTypeCount = IntegerMapUtils.increment(coordinateTypeCountMap, pair, 0);
                        return createWriterForCoordinate(
                                fileInformation, layerDataType, c, coordinateTypeCount, tppDatasetFolder, destinationEncoding
                        );
                    });

                    SimpleFeature newFeature = feature;
                    if (cutFeatures) {
                        newFeature = FeatureGeometryUtils.getFeatureGeometryIntersection(
                                feature, coordinate.getTileGeometry(buffer)
                        );
                    }
                    if (newFeature != null) {
                        writer.write(newFeature);
                        writer.flush();
                    }
                }
                progressBar.step();
            }
            featureIterator.close();

            progressBar.setExtraMessage("Writing...");
            for (Map.Entry<TppTileCoordinate, SimpleFeatureJsonWriter> entry : writerMap.entrySet()) {
                entry.getValue().close();
            }
            progressBar.close();

            return null;
        });
    }


    private static SimpleFeatureJsonWriter createWriterForCoordinate(ScjdFileInformation fileInformation,
                                                                     LayerDataType layerDataType,
                                                                     TppTileCoordinate tileCoordinate,
                                                                     int count,
                                                                     File datasetFolder, Charset encoding) {
        try {
            File coordinateFolder = tileCoordinate.getFolderLocation(datasetFolder, true);
            String fileName = String.format(
                    "%s_%s_%d.json", fileInformation.getNameForFile(), layerDataType.getLayerName(), count
            );
            return new SimpleFeatureJsonWriter(new File(coordinateFolder, fileName), encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
