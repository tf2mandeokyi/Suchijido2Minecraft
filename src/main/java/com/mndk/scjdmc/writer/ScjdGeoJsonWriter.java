package com.mndk.scjdmc.writer;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.file.DirectoryManager;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class ScjdGeoJsonWriter {


    public static void writeFeatureMapFolder(
            ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap, File destinationParent
    ) throws IOException {
        String featureMapName = featureMap.getFileInformation().getNameForFile();

        File destinationFolder = new File(destinationParent, featureMapName);
        writeFeatureMap(featureMap, destinationFolder);
    }


    public static void writeFeatureMap(
            ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap, File destinationFolder
    ) throws IOException {
        for(Map.Entry<LayerDataType, List<SimpleFeatureCollection>> entry : featureMap.entrySet()) {
            String jsonFileName = entry.getKey().getLayerName() + ".json";
            List<SimpleFeatureCollection> featureCollection = entry.getValue();

            if(featureCollection != null) {
                writeCollection(entry.getValue(), new File(destinationFolder, jsonFileName));
            }
        }
    }


    public static void writeCollection(
            List<SimpleFeatureCollection> collections, File destinationFile
    ) throws IOException {
        DirectoryManager.createParentFolders(destinationFile);
        try (FileWriter writer = new FileWriter(destinationFile)) {
            writeCollection(collections, writer);
            writer.flush();
        }
    }

    public static void writeCollection(List<SimpleFeatureCollection> collections, Writer writer) throws IOException {

        boolean first = true;
        writer.write(Constants.GEOJSON_BEGINNING);

        for(SimpleFeatureCollection collection : collections) {
            if(collection == null) continue;

            SimpleFeatureIterator featureIterator = collection.features();
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                if (first) first = false;
                else writer.write(",");

                writer.write(Constants.FEATURE_JSON.toString(feature));
            }
        }

        writer.write(Constants.GEOJSON_END);
    }

}
