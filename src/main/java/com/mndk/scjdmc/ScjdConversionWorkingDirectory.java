package com.mndk.scjdmc;

import com.mndk.scjdmc.reader.GeoJsonDirScjdReader;
import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.relocator.Scjd2TppDatasetRelocator;
import com.mndk.scjdmc.relocator.ScjdCoastlineRelocator;
import com.mndk.scjdmc.scissor.ScjdOsmFeatureScissor;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.typeconverter.Scjd2OsmFeatureConverter;
import com.mndk.scjdmc.util.ProgressBarUtils;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.TppTileCoordinate;
import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.writer.ScjdGeoJsonWriter;
import lombok.Getter;
import lombok.Setter;
import me.tongfei.progressbar.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Getter
public class ScjdConversionWorkingDirectory {


    private static final Logger LOGGER = LogManager.getLogger();


    @Getter
    private final File masterDirectory;

    @Getter
    private final File indexFolder, areaFolder, scjdGeojsonFolder, tppGeoJsonFolder, tiffFolder;

    @Setter
    private boolean debug = false;


    public ScjdConversionWorkingDirectory(File masterDirectory) throws IOException {
        this.masterDirectory = masterDirectory;

        this.indexFolder = DirectoryManager.createFolder(new File(masterDirectory, "indexes"));
        this.areaFolder = DirectoryManager.createFolder(new File(masterDirectory, "areas"));
        this.scjdGeojsonFolder = DirectoryManager.createFolder(new File(masterDirectory, "geojson_scjd"));
        this.tppGeoJsonFolder = DirectoryManager.createFolder(new File(masterDirectory, "geojson_tpp"));
        this.tiffFolder = DirectoryManager.createFolder(new File(masterDirectory, "tiff"));
    }


    public void relocateAllIndexes() throws IOException {
        this.doSimpleRelocation(this.indexFolder, ScjdParsedType.INDEX);
    }
    public void relocateAllAreas() throws IOException {
        this.doSimpleRelocation(this.areaFolder, ScjdParsedType.AREA);
    }


    public void combineGeoJsonFiles() throws IOException {
        Set<TppTileCoordinate> coordinates = TppTileCoordinate.getAvailableFolderCoordinates(this.tppGeoJsonFolder);
        ScjdDatasetReader reader = new GeoJsonDirScjdReader();
        reader.setLayerFilter(ScjdConversionWorkingDirectory::relocationLayerFilter);

        ProgressBar progressBar = ProgressBarUtils.createProgressBar("Combining geojson files", coordinates.size());

//        int i = 0;
        for(TppTileCoordinate coordinate : coordinates) {
            progressBar.step();

//            i++;
//            if(i < 20607) continue;
//            if(!new TppTileCoordinate(8131, 2400).equals(coordinate)) continue;
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


    public void doCoastlineRelocation() throws IOException {
        ScjdCoastlineRelocator.relocate(this.areaFolder, Constants.CP949, this.tppGeoJsonFolder);
    }


    public void convertAllIndexes() throws IOException {
        this.doSimpleConversion(this.indexFolder, ScjdParsedType.INDEX);
    }
    public void convertAllAreas() throws IOException {
        this.doSimpleConversion(this.areaFolder, ScjdParsedType.AREA);
    }


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
