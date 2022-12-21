package com.mndk.scjdmc.relocator;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.*;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
            SimpleFeatureIterator featureIterator = featureCollection.features();
            Map<TppTileCoordinate, Writer> writerMap = new HashMap<>();

            try (ProgressBar progressBar = createProgressBar(fileInformation, layerDataType, featureCollection.size() + 1)) {
                while(featureIterator.hasNext()) {
                    SimpleFeature feature = featureIterator.next();
                    String json = Constants.FEATURE_JSON.toString(feature);

                    BoundingBox featureBoundingBox = feature.getBounds();
                    Set<TppTileCoordinate> tileCoordinates = TppTileCoordinate.getBoundingBoxIntersections(featureBoundingBox);

                    for(TppTileCoordinate coordinate : tileCoordinates) {
                        Pair<TppTileCoordinate, LayerDataType> pair = Pair.of(coordinate, layerDataType);
                        Writer writer;

                        if(!writerMap.containsKey(coordinate)) {
                            int count = IntegerMapUtils.increment(countMap, pair, 0);
                            writer = createWriterForCoordinate(
                                    fileInformation, layerDataType, coordinate, count, tppDatasetFolder
                            );
                            writerMap.put(coordinate, writer);
                            writer.write(Constants.GEOJSON_BEGINNING);
                        } else {
                            writer = writerMap.get(coordinate);
                            writer.write(",");
                        }
                        writer.write(json);
                    }
                    progressBar.step();
                }
                featureIterator.close();

                progressBar.setExtraMessage("Writing...");
                for(Map.Entry<TppTileCoordinate, Writer> entry : writerMap.entrySet()) {
                    Writer writer = entry.getValue();
                    writer.write(Constants.GEOJSON_END);
                    writer.flush();
                    writer.close();
                }
                progressBar.step();
            }

            return null;
        });
    }


    private static Writer createWriterForCoordinate(
            ScjdFileInformation fileInformation, LayerDataType layerDataType, TppTileCoordinate tileCoordinate,
            int count, File datasetFolder
    ) throws IOException {
        File coordinateFolder = tileCoordinate.getFolderLocation(datasetFolder, true);
        String fileName = String.format(
                "%s_%s_%d.json", fileInformation.getNameForFile(), layerDataType.getLayerName(), count
        );
        File resultFile = new File(coordinateFolder, fileName);
        return new OutputStreamWriter(Files.newOutputStream(resultFile.toPath()), StandardCharsets.UTF_8);
    }


    private static ProgressBar createProgressBar(ScjdFileInformation fileInformation, LayerDataType layerDataType, int size) {
        return ProgressBarUtils.createProgressBar(
                String.format("Relocating: %s_%s", fileInformation.getNameForFile(), layerDataType), size
        );
    }

}
