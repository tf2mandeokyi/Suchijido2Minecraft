package com.mndk.scjdmc.writer;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.ParsedOsmFeatureMap;
import com.mndk.scjdmc.util.file.DirectoryManager;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class ScjdGeoJsonWriter {


    public static void writeFeatureMapFolder(
            ParsedOsmFeatureMap featureMap, File destinationParent
    ) throws IOException {
        File destinationFolder = new File(destinationParent, featureMap.getName());
        writeFeatureMap(featureMap, destinationFolder);
    }


    public static void writeFeatureMap(
            ParsedOsmFeatureMap featureMap, File destinationFolder
    ) throws IOException {
        for(Map.Entry<LayerDataType, SimpleFeatureCollection> entry : featureMap.entrySet()) {
            String jsonFileName = entry.getKey().getLayerNameHeader() + "0000.json";
            writeCollection(entry.getValue(), new File(destinationFolder, jsonFileName));
        }
    }


    public static void writeCollection(
            SimpleFeatureCollection collection, File destinationFile
    ) throws IOException {
        DirectoryManager.createParentFolders(destinationFile);
        try (FileWriter writer = new FileWriter(destinationFile)) {
            writeCollection(collection, writer);
            writer.flush();
        }
    }

    public static void writeCollection(SimpleFeatureCollection collection, Writer writer) throws IOException {

        boolean first = true;
        List<Geometry> subtractGeometries;
        writer.write("{\"type\":\"FeatureCollection\",\"features\":[");

        boolean firstFeature = true;
        SimpleFeatureIterator featureIterator = collection.features();
        while(featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();
            if(first) first = false;
            else writer.write(",");

            writer.write(Constants.FEATURE_JSON.toString(feature));
        }

        writer.write("]}");
    }

}
