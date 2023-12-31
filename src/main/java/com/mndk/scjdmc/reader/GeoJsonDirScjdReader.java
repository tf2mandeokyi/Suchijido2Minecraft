package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.file.ScjdFileInformation;
import com.mndk.scjdmc.util.ScjdParsedType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

public class GeoJsonDirScjdReader extends ScjdDatasetReader {


    private static final Logger LOGGER = LogManager.getLogger();


    @Override
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

        File[] geojsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
        assert geojsonFiles != null;

        if(geojsonFiles.length == 0) {
            LOGGER.warn("No .json file found in " + directory.getName());
        }

        ScjdFileInformation fileInformation = new ScjdFileInformation(directory, parsedType);

        ScjdDirectoryParsedMap<T> result = new ScjdDirectoryParsedMap<>(fileInformation);
        for(File geojsonFile : geojsonFiles) {

            if(Constants.COASTLINE_GEOMETRY_FILE_NAME.equals(geojsonFile.getName())) {
                Reader reader = new FileReader(geojsonFile, charset);
                Geometry coastlineGeometry = Constants.GEOMETRY_JSON.read(reader);
                result.setCoastlineGeometry(coastlineGeometry);
                reader.close();
                continue;
            }

            if(!geojsonFile.getName().startsWith(ScjdParsedType.AREA + "_")) continue;

            LayerDataType layerDataType = LayerDataType.fromLayerName(geojsonFile.getName());
            if(!this.layerFilter.apply(layerDataType)) continue;

            T geojsonReadResult = GeoJsonScjdReader.read(geojsonFile, charset, featureCollectionFunction);
            result.put(layerDataType, geojsonReadResult);
        }

        return result;
    }
}
