package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.file.ScjdFileInformation;
import com.mndk.scjdmc.util.ScjdParsedType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ShpDirScjdReader extends ScjdDatasetReader {


    private static final Logger LOGGER = LogManager.getLogger();


    public <T> ScjdDirectoryParsedMap<T> read(
            File directory, Charset charset, ScjdParsedType parsedType,
            FCFunction<T> featureCollectionFunction
    ) throws IOException {
        if(!directory.exists()) {
            throw new IOException("Path doesn't exist: " + directory.getPath());
        }
        if(!directory.isDirectory()) {
            throw new IOException("Path should be directory: " + directory.getPath());
        }

        File[] shpFiles = directory.listFiles((dir, name) -> name.endsWith(".shp"));
        assert shpFiles != null;

        if(shpFiles.length == 0) {
            LOGGER.warn("No .shp file found in " + directory.getName());
        }

        ScjdFileInformation fileInformation = new ScjdFileInformation(directory, parsedType);

        ScjdDirectoryParsedMap<T> result = new ScjdDirectoryParsedMap<>(fileInformation);
        for(File shpFile : shpFiles) {
            LayerDataType layerDataType = LayerDataType.fromLayerName(shpFile.getName());
            if(!this.layerFilter.apply(layerDataType)) continue;
            T shpReadResult = ShpScjdReader.read(shpFile, fileInformation, charset, featureCollectionFunction);
            result.put(layerDataType, shpReadResult);
        }
        return result;
    }
}
