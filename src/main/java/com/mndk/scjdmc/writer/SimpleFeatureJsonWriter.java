package com.mndk.scjdmc.writer;

import com.mndk.scjdmc.Constants;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SimpleFeatureJsonWriter implements Closeable {

    private boolean first = true;
    private final Writer writer;
    private final String endString;
    private String cacheString;
    private Object cacheObject;

    public static SimpleFeatureJsonWriter newFeatureCollectionWriter(File file) throws IOException {
        return new SimpleFeatureJsonWriter(
                file,
                "{\"type\":\"FeatureCollection\",\"features\":[",
                "]}"
        );
    }

    private SimpleFeatureJsonWriter(File file, String start, String endString) throws IOException {
        this.writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
        this.endString = endString;
        writer.write(start);
    }

    public void write(SimpleFeature feature) throws IOException {
        if (first) first = false;
        else writer.write(",");

        if(feature != this.cacheObject) {
            this.cacheString = Constants.FEATURE_JSON.toString(feature);
            this.cacheObject = feature;
        }
        writer.write(this.cacheString);
    }

    @Override
    public void close() throws IOException {
        writer.write(this.endString);
        writer.close();
    }
}
