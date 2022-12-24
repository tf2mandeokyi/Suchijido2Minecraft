package com.mndk.scjdmc;

import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.reader.GeoJsonDirScjdReader;
import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.relocator.Scjd2TppDatasetRelocator;
import com.mndk.scjdmc.relocator.ScjdCoastlineRelocator;
import com.mndk.scjdmc.scissor.ScjdOsmFeatureScissor;
import com.mndk.scjdmc.terrain.ScjdContour2RasterConverter;
import com.mndk.scjdmc.typeconverter.Scjd2OsmFeatureConverter;
import com.mndk.scjdmc.util.ProgressBarUtils;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.TppTileCoordinate;
import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.util.math.Vector2DH;
import com.mndk.scjdmc.writer.ScjdGeoJsonWriter;
import lombok.Getter;
import lombok.Setter;
import me.tongfei.progressbar.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Getter
public class ScjdConversionWorkingDirectory {


    private static final Logger LOGGER = LogManager.getLogger();


    @Getter
    private final File masterDirectory;

    @Getter
    private final File indexDirectory, areaDirectory, scjdGeojsonFolder, tppGeoJsonFolder, tiffDirectory;

    @Setter
    private boolean debug = false;


    public ScjdConversionWorkingDirectory(File masterDirectory) throws IOException {
        this.masterDirectory = masterDirectory;

        this.indexDirectory = DirectoryManager.createFolder(new File(masterDirectory, "indexes"));
        this.areaDirectory = DirectoryManager.createFolder(new File(masterDirectory, "areas"));
        this.scjdGeojsonFolder = DirectoryManager.createFolder(new File(masterDirectory, "geojson_scjd"));
        this.tppGeoJsonFolder = DirectoryManager.createFolder(new File(masterDirectory, "geojson_tpp"));
        this.tiffDirectory = DirectoryManager.createFolder(new File(masterDirectory, "tiff"));
    }


    public void terrainTest() throws IOException {
        File[] areaFolders = this.areaDirectory.listFiles(File::isDirectory);
        assert areaFolders != null;

        for(File areaFolder : areaFolders) {
            ScjdDatasetReader reader = ScjdDatasetReader.getShpReader(areaFolder);
            reader.setLayerFilter(type -> type == LayerDataType.등고선 || type == LayerDataType.표고점);
            reader.read(areaFolder, Constants.CP949, ScjdParsedType.AREA, (featureCollection, layerDataType) -> {
                List<Vector2DH[]> vectors = ScjdContour2RasterConverter.elevationObjectToVectors(
                        featureCollection, layerDataType
                );
                // TODO do something here
                return null;
            });
        }
    }


    public void combineGeoJsonFiles() throws IOException {
        Set<TppTileCoordinate> coordinates = TppTileCoordinate.getAvailableFolderCoordinates(this.tppGeoJsonFolder);
        ScjdDatasetReader reader = new GeoJsonDirScjdReader();
        reader.setLayerFilter(ScjdConversionWorkingDirectory::relocationLayerFilter);

        ProgressBar progressBar = ProgressBarUtils.createProgressBar("Combining geojson files", coordinates.size());

        for(TppTileCoordinate coordinate : coordinates) {
            progressBar.step();

            Constants.STACKED_THROWABLES.clear();

            File coordinateFolder = coordinate.getFolderLocation(this.tppGeoJsonFolder, false);
            ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap = Scjd2OsmFeatureConverter.parseAsOsmFeature(
                    coordinateFolder, Constants.CP949, ScjdParsedType.TILE, reader, feature -> true
            );
            featureMap = ScjdOsmFeatureScissor.apply(featureMap, coordinate.getTileGeometry(0.1));

            ScjdGeoJsonWriter.writeAsSingleJsonFile(
                    featureMap,
                    coordinate.getJsonLocation(this.tppGeoJsonFolder, true)
            );

            if(!Constants.STACKED_THROWABLES.isEmpty()) {
                for(Throwable e : Constants.STACKED_THROWABLES) {
                    LOGGER.error("Error caught while parsing " + coordinate, e);
                }
            }
        }
        progressBar.close();
    }


    public void doCoastlineRelocation() throws IOException {
        ScjdCoastlineRelocator.relocate(this.areaDirectory, Constants.CP949, this.tppGeoJsonFolder);
    }


    public void relocateAllIndexes() throws IOException {
        this.doSimpleRelocation(this.indexDirectory, ScjdParsedType.INDEX);
    }
    public void relocateAllAreas() throws IOException {
        this.doSimpleRelocation(this.areaDirectory, ScjdParsedType.AREA);
    }
    public void doSimpleRelocation(File sourceDir, ScjdParsedType parsedType) throws IOException {
        File[] sourceFiles = sourceDir.listFiles();
        assert sourceFiles != null;

        for(File sourceFile : sourceFiles) {
            if(debug) System.out.println("Relocating " + sourceFile.getName() + "...");

            ScjdDatasetReader reader = ScjdDatasetReader.getShpReader(sourceFile);
            reader.setLayerFilter(ScjdConversionWorkingDirectory::relocationLayerFilter);
            Scjd2TppDatasetRelocator.relocate(sourceFile, Constants.CP949, parsedType, reader, this.tppGeoJsonFolder);
        }
    }


    @Deprecated
    public void convertAllIndexes() throws IOException {
        this.doSimpleConversion(this.indexDirectory, ScjdParsedType.INDEX);
    }
    @Deprecated
    public void convertAllAreas() throws IOException {
        this.doSimpleConversion(this.areaDirectory, ScjdParsedType.AREA);
    }
    @Deprecated
    private void doSimpleConversion(File sourceDir, ScjdParsedType parsedType) throws IOException {
        File[] sourceFiles = sourceDir.listFiles();
        assert sourceFiles != null;

        for(File sourceFile : sourceFiles) {
            if(debug) LOGGER.info("Converting {}...", sourceFile.getName());

            ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap =
                    Scjd2OsmFeatureConverter.parseShpAsOsmFeature(sourceFile, Constants.CP949, parsedType);
            featureMap = ScjdOsmFeatureScissor.apply(featureMap, null);

            String featureMapName = featureMap.getFileInformation().getNameForFile();
            File destinationFolder = new File(this.scjdGeojsonFolder, featureMapName);
            ScjdGeoJsonWriter.writeAsFolder(featureMap, destinationFolder);
        }
    }


    private static boolean relocationLayerFilter(LayerDataType layerDataType) {
        LayerDataType.Category c = layerDataType.getCategory();
        return
                layerDataType.hasElementClass() &&
                layerDataType != LayerDataType.도로중심선 && // Why need center lines when we have road areas?
                layerDataType != LayerDataType.해안선 &&
                c != LayerDataType.Category.지형 &&
                c != LayerDataType.Category.경계 &&
                c != LayerDataType.Category.주기;
    }

}
