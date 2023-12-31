package com.mndk.scjdmc;

import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.combiner.ScjdGeoJsonTileCombiner;
import com.mndk.scjdmc.gui.ProgressGui;
import com.mndk.scjdmc.reader.GeoJsonDirScjdReader;
import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.relocator.Scjd2TppDatasetRelocator;
import com.mndk.scjdmc.relocator.ScjdCoastlineRelocator;
import com.mndk.scjdmc.util.ProgressBarUtils;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.TppTileCoordinate;
import com.mndk.scjdmc.util.file.DirectoryManager;
import lombok.Getter;
import lombok.Setter;
import me.tongfei.progressbar.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

        if(debug) LOGGER.info("Relocating contours & elevation points...");
        for(File areaFolder : areaFolders) {
            if(!"gyeongbuk".equals(areaFolder.getName())) continue;
            ScjdDatasetReader reader = ScjdDatasetReader.getShpReader(areaFolder);
            reader.setLayerFilter(type -> type == LayerDataType.등고선 || type == LayerDataType.표고점);

            Scjd2TppDatasetRelocator.relocate(
                    areaFolder, Constants.CP949, ScjdParsedType.AREA, reader,
                    this.scjdGeojsonFolder, StandardCharsets.UTF_8,
                    0.1, true
            );
        }
    }


    public void convertScjdGeoJsonToOsmGeoJson() throws IOException {

        if(debug) LOGGER.info("Converting individual scjd geojson fileset into single osm style geojson file...");

        Set<TppTileCoordinate> coordinates = TppTileCoordinate.getAvailableFolderCoordinates(this.scjdGeojsonFolder);
        GeoJsonDirScjdReader reader = new GeoJsonDirScjdReader();
        reader.setLayerFilter(ScjdConversionWorkingDirectory::relocationLayerFilter);
        ProgressGui gui = new ProgressGui(1000, 1000);
        for(TppTileCoordinate coordinate : coordinates) {
            gui.setStatus(coordinate.getBoundingBox(), Color.WHITE);
        }

        if(debug) LOGGER.info("Conversion started");

        ProgressBar progressBar = ProgressBarUtils.createProgressBar("Combining geojson files", coordinates.size());
        for(TppTileCoordinate coordinate : coordinates) {
            progressBar.step();

            int count = ScjdGeoJsonTileCombiner.combine(
                    this.scjdGeojsonFolder, StandardCharsets.UTF_8,
                    this.tppGeoJsonFolder, StandardCharsets.UTF_8,
                    coordinate, reader
            );
            if(!Constants.STACKED_THROWABLES.isEmpty()) {
                gui.setStatus(coordinate.getBoundingBox(), Color.RED);
            } else if(count == 0) {
                gui.setStatus(coordinate.getBoundingBox(), Color.YELLOW);
            } else {
                gui.setStatus(coordinate.getBoundingBox(), Color.GREEN);
            }
            Constants.STACKED_THROWABLES.popAllToLogger(LOGGER, "Error(s) caught while parsing " + coordinate);
        }
        progressBar.close();
    }


    public void doCoastlineRelocation() throws IOException {
        ScjdCoastlineRelocator.relocate(this.areaDirectory, Constants.CP949, this.scjdGeojsonFolder, StandardCharsets.UTF_8, this.debug);
    }


    public void relocateAllAreas() throws IOException {
        this.doSimpleRelocation(this.areaDirectory, Constants.CP949, ScjdParsedType.AREA, this.scjdGeojsonFolder, StandardCharsets.UTF_8);
    }
    public void doSimpleRelocation(File sourceDir, Charset sourceEncoding, ScjdParsedType parsedType,
                                   File destinationDir, Charset destinationEncoding) throws IOException {
        File[] sourceFiles = sourceDir.listFiles();
        assert sourceFiles != null;

//        boolean start = false;
        for(File sourceFile : sourceFiles) {
//            if("jeonbuk".equals(sourceFile.getName())) start = true;
//            if(!start) continue;

            if(debug) LOGGER.info("Relocating {}...", sourceFile.getName());

            ScjdDatasetReader reader = ScjdDatasetReader.getShpReader(sourceFile);
            if(reader == null) {
                LOGGER.warn("Could not find suitable reader for " + sourceFile.getName());
                continue;
            }

            reader.setLayerFilter(ScjdConversionWorkingDirectory::relocationLayerFilter);
            Scjd2TppDatasetRelocator.relocate(
                    sourceFile, sourceEncoding, parsedType, reader,
                    destinationDir, destinationEncoding,
                    0, false
            );
        }
    }


    private static boolean relocationLayerFilter(LayerDataType layerDataType) {
        LayerDataType.Category c = layerDataType.getCategory();
        return layerDataType.hasElementClass() &&
                layerDataType != LayerDataType.도로중심선 && // Why need center lines when we have road areas?
                layerDataType != LayerDataType.해안선 &&
                c != LayerDataType.Category.지형 &&
                c != LayerDataType.Category.경계 &&
                c != LayerDataType.Category.주기;
    }

}
