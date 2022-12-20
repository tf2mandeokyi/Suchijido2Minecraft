package com.mndk.scjdmc;

import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.relocator.Scjd2TppDatasetRelocator;
import com.mndk.scjdmc.scissor.ScjdOsmFeatureScissor;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.typeconverter.Scjd2OsmFeature;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.writer.ScjdGeoJsonWriter;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.io.File;
import java.io.IOException;

@Getter
public class ScjdConversionWorkingDirectory {


    private static final Logger LOGGER = LogManager.getLogger();


    @Getter
    private final File masterDirectory;

    @Getter
    private final File indexFolder, areaFolder, scjdGeojsonFolder, tppGeoJsonFolder, tiffFolder;

    @Setter
    private boolean debug = false, stopIfError = false;


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


    private static boolean relocationLayerFilter(LayerDataType layerDataType) {
        LayerDataType.Category c = layerDataType.getCategory();
        return
                layerDataType.hasElementClass() &&
                c != LayerDataType.Category.지형 &&
                c != LayerDataType.Category.경계 &&
                c != LayerDataType.Category.주기;
    }

    public void doSimpleRelocation(File sourceDir, ScjdParsedType parsedType) throws IOException {
        File[] sourceFiles = sourceDir.listFiles();
        assert sourceFiles != null;

        for(File sourceFile : sourceFiles) {
            if(debug) System.out.println("Relocating " + sourceFile.getName() + "...");

            try {
                ScjdDatasetReader reader = ScjdDatasetReader.getShpReader(sourceFile);
                reader.setLayerFilter(ScjdConversionWorkingDirectory::relocationLayerFilter);
                Scjd2TppDatasetRelocator.relocate(sourceFile, Constants.CP949, parsedType, reader, this.tppGeoJsonFolder);

            } catch(IOException e) {
                if(stopIfError) throw e;
                LOGGER.error("IOException thrown while converting: " + sourceFile, e);
            }
        }
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

            try {
                ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap =
                        Scjd2OsmFeature.parseAsOsmFeature(sourceFile, Constants.CP949, parsedType);
                featureMap = ScjdOsmFeatureScissor.apply(featureMap);

                ScjdGeoJsonWriter.writeFeatureMapFolder(featureMap, this.scjdGeojsonFolder);
            } catch(IOException e) {
                if(stopIfError) throw e;
                LOGGER.error("IOException thrown while converting: " + sourceFile, e);
            }
        }
    }

}
