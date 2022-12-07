package com.mndk.scjdmc;

import com.mndk.scjdmc.elementrelocator.ScjdElementRelocator;
import com.mndk.scjdmc.scissor.ScjdOsmFeatureScissor;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.typeconverter.Scjd2OsmFeature;
import com.mndk.scjdmc.util.ParsedOsmFeatureMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.writer.ScjdGeoJsonWriter;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private ScjdElementRelocator elementRelocator;

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


    public void convertAllIndexes() throws IOException {
        LayerDataType.도로경계.getElementClass();

        File[] files = this.indexFolder.listFiles();
        assert files != null;

        for(File file : files) {
            if(file.isDirectory()) continue;

            if(debug) LOGGER.info("Converting {}...", file.getName());

            ParsedOsmFeatureMap featureMap = Scjd2OsmFeature.parseAsOsmFeature(file, "CP949", ScjdParsedType.INDEX);
            featureMap = ScjdOsmFeatureScissor.apply(featureMap);

            ScjdGeoJsonWriter.writeFeatureMapFolder(featureMap, this.scjdGeojsonFolder);
        }
    }

}
