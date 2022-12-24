package com.mndk.scjdmc.relocator;

import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.*;
import com.mndk.scjdmc.util.file.ScjdFileInformation;
import com.mndk.scjdmc.writer.SimpleFeatureJsonWriter;
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


    public static void relocate(
            File sourceFile, Charset charset, ScjdParsedType parsedType,
            ScjdDatasetReader reader, File tppDatasetFolder
    ) throws IOException {

        Map<Pair<TppTileCoordinate, LayerDataType>, Integer> countMap = new HashMap<>();
        ScjdFileInformation fileInformation = new ScjdFileInformation(sourceFile, parsedType);

        reader.read(sourceFile, charset, parsedType, (featureCollection, layerDataType) -> {
            Map<TppTileCoordinate, SimpleFeatureJsonWriter> writerMap = new HashMap<>();

            try (
                    SimpleFeatureIterator featureIterator = featureCollection.features();
                    ProgressBar progressBar =
                            createProgressBar(fileInformation, layerDataType, featureCollection.size() + 1)
            ) {
                while(featureIterator.hasNext()) {
                    SimpleFeature feature = featureIterator.next();

                    BoundingBox featureBoundingBox = feature.getBounds();
                    Set<TppTileCoordinate> tileCoordinates = TppTileCoordinate.getBoundingBoxIntersections(featureBoundingBox);

                    for(TppTileCoordinate coordinate : tileCoordinates) {
                        Pair<TppTileCoordinate, LayerDataType> pair = Pair.of(coordinate, layerDataType);
                        SimpleFeatureJsonWriter writer;

                        if(!writerMap.containsKey(coordinate)) {
                            int count = IntegerMapUtils.increment(countMap, pair, 0);
                            writer = createWriterForCoordinate(
                                    fileInformation, layerDataType, coordinate, count, tppDatasetFolder
                            );
                            writerMap.put(coordinate, writer);
                        } else {
                            writer = writerMap.get(coordinate);
                        }
                        writer.write(feature);
                    }
                    progressBar.step();
                }
                featureIterator.close();

                progressBar.setExtraMessage("Writing...");
                for(Map.Entry<TppTileCoordinate, SimpleFeatureJsonWriter> entry : writerMap.entrySet()) {
                    entry.getValue().close();
                }
                progressBar.step();
            }

            return null;
        });
    }


    private static SimpleFeatureJsonWriter createWriterForCoordinate(
            ScjdFileInformation fileInformation, LayerDataType layerDataType, TppTileCoordinate tileCoordinate,
            int count, File datasetFolder
    ) throws IOException {
        File coordinateFolder = tileCoordinate.getFolderLocation(datasetFolder, true);
        String fileName = String.format(
                "%s_%s_%d.json", fileInformation.getNameForFile(), layerDataType.getLayerName(), count
        );
        return SimpleFeatureJsonWriter.newFeatureCollectionWriter(new File(coordinateFolder, fileName));
    }


    private static ProgressBar createProgressBar(
            ScjdFileInformation fileInformation, LayerDataType layerDataType, int size
    ) {
        return ProgressBarUtils.createProgressBar(
                String.format("Relocating: %s_%s", fileInformation.getNameForFile(), layerDataType), size
        );
    }

}
