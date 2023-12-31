package com.mndk.scjdmc.writer;

import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.file.DirectoryManager;
import com.mndk.scjdmc.util.io.SimpleFeatureJsonWriter;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class ScjdGeoJsonWriter {


    public static void writeAsSingleJsonFile(ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap,
                                             File destinationFile, Charset encoding) throws IOException {

        DirectoryManager.createParentFolders(destinationFile);
        try (SimpleFeatureJsonWriter writer = new SimpleFeatureJsonWriter(destinationFile, encoding)) {
            for(Map.Entry<LayerDataType, List<SimpleFeatureCollection>> entry : featureMap.entrySet()) {
                List<SimpleFeatureCollection> featureCollection = entry.getValue();
                if(featureCollection != null) writeCollectionList(entry.getValue(), writer);
            }
        }
    }


    public static void writeAsFolder(ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap,
                                     File destinationFolder, Charset destinationEncoding) throws IOException {

        for(Map.Entry<LayerDataType, List<SimpleFeatureCollection>> entry : featureMap.entrySet()) {
            String jsonFileName = entry.getKey().getLayerName() + ".json";
            List<SimpleFeatureCollection> featureCollection = entry.getValue();
            if(featureCollection == null) continue;

            File destinationFile = new File(destinationFolder, jsonFileName);
            DirectoryManager.createParentFolders(destinationFile);

            try (SimpleFeatureJsonWriter writer = new SimpleFeatureJsonWriter(destinationFile, destinationEncoding)) {
                writeCollectionList(entry.getValue(), writer);
            }
        }
    }


    public static void writeCollectionList(
            List<SimpleFeatureCollection> collections, SimpleFeatureJsonWriter writer
    ) throws IOException {
        for(SimpleFeatureCollection collection : collections) {
            if(collection == null) continue;

            SimpleFeatureIterator featureIterator = collection.features();
            while (featureIterator.hasNext()) {
                writer.write(featureIterator.next());
            }
            featureIterator.close();
        }

    }

}
