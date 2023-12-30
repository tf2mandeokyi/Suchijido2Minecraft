package com.mndk.scjdmc.relocator;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.reader.ShpDirScjdReader;
import com.mndk.scjdmc.util.ProgressBarUtils;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.TppTileCoordinate;
import me.tongfei.progressbar.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScjdCoastlineRelocator {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void relocate(File areaShpDirectory, Charset charset, File destinationFolder, boolean debug) throws IOException {
        Set<TppTileCoordinate> boundaryWrittenCoordinates = writeTileBoundaryIntersections(areaShpDirectory, charset, destinationFolder, debug);
        createCoastlineWithBoundary(boundaryWrittenCoordinates, destinationFolder, debug);
    }


    private static void createCoastlineWithBoundary(
            Set<TppTileCoordinate> boundaryWrittenCoordinates, File destinationFolder, boolean debug
    ) throws IOException {

        if(debug) LOGGER.info("Writing coastlines...");

        ProgressBar progressBar = createCoastlineWritingProgressBar(boundaryWrittenCoordinates.size());
        for(TppTileCoordinate coordinate : boundaryWrittenCoordinates) {
            Geometry coastlineGeometry = coordinate.getTileGeometry(0.1);

            File folderLocation = coordinate.getFolderLocation(destinationFolder, true);
            File[] boundaryFiles = folderLocation.listFiles(f -> f.getName().matches("boundary_(.+)\\.json$"));
            assert boundaryFiles != null;

            for(File boundaryFile : boundaryFiles) {
                Geometry boundaryGeometry = Constants.GEOMETRY_JSON.read(Files.newInputStream(boundaryFile.toPath()));
                coastlineGeometry = coastlineGeometry.difference(boundaryGeometry.buffer(0));
            }

            if(coastlineGeometry.isEmpty()) continue;

            try(Writer writer = new FileWriter(new File(folderLocation, Constants.COASTLINE_GEOMETRY_FILE_NAME))) {
                writer.write(Constants.GEOMETRY_JSON.toString(coastlineGeometry));
            }

            progressBar.step();
        }
        progressBar.close();

    }


    private static Set<TppTileCoordinate> writeTileBoundaryIntersections(
            File areaShpDirectory, Charset charset, File destinationFolder, boolean debug
    ) throws IOException {

        Set<TppTileCoordinate> result = new HashSet<>();

        File[] areaShpFolders = areaShpDirectory.listFiles(File::isDirectory);
        assert areaShpFolders != null;

        ScjdDatasetReader reader = new ShpDirScjdReader();
        reader.setLayerFilter(layer -> layer == LayerDataType.시도_행정경계);

        for(File areaShpFolder : areaShpFolders) {

            if(debug) LOGGER.info("Writing boundary features of {}...", areaShpFolder.getName());

            reader.read(areaShpFolder, charset, ScjdParsedType.AREA, (featureCollection, layerDataType) -> {
                SimpleFeatureIterator boundaryFeatures = featureCollection.features();

                for(int i = 0; boundaryFeatures.hasNext(); i++) {
                    SimpleFeature feature = boundaryFeatures.next();
                    Map<TppTileCoordinate, Geometry> divisionMap =
                            TppTileCoordinate.divideFeatureGeometryToTiles(feature, Constants.POLYGON_BUFFER_EPSILON, 0.11);
                    result.addAll(divisionMap.keySet());
                    writeCoordinateGeometryMap(areaShpFolder, divisionMap, i, featureCollection.size(), destinationFolder);
                }

                boundaryFeatures.close();
                return null;
            });
        }

        return result;
    }


    private static void writeCoordinateGeometryMap(
            File areaShpFolder, Map<TppTileCoordinate, Geometry> divisionMap,
            int featureIndex, int featureCollectionSize, File destinationFolder
    ) throws IOException {
        ProgressBar progressBar = createBoundaryWritingProgressBar(
                areaShpFolder, featureIndex, featureCollectionSize, divisionMap.size()
        );

        for (Map.Entry<TppTileCoordinate, Geometry> entry : divisionMap.entrySet()) {
            TppTileCoordinate coordinate = entry.getKey();
            Geometry intersection = entry.getValue().buffer(0);

            File file = new File(
                    coordinate.getFolderLocation(destinationFolder, true),
                    "boundary_" + areaShpFolder.getName() + "_" + featureIndex + ".json"
            );
            Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()),
                    StandardCharsets.UTF_8);
            writer.write(Constants.GEOMETRY_JSON.toString(intersection));
            writer.close();
            progressBar.step();
        }
        progressBar.close();
    }


    private static ProgressBar createCoastlineWritingProgressBar(int size) {
        return ProgressBarUtils.createProgressBar("Writing coastlines", size);
    }
    private static ProgressBar createBoundaryWritingProgressBar(
            File areaShpFolder, int featureIndex, int featureCount, int size
    ) {
        return ProgressBarUtils.createProgressBar(
                String.format("Writing boundary feature %d/%d of \"%s\"", featureIndex, featureCount, areaShpFolder.getName()),
                size
        );
    }

}
