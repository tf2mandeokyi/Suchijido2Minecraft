package com.mndk.scjdmc.util.io;

import com.mndk.scjdmc.Constants;
import lombok.NonNull;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class SimpleFeatureJsonWriter implements Closeable {

    private static String CACHE_STRING;
    private static Object CACHE_OBJECT;

    private boolean first = true;
    private final Writer writer;

    public SimpleFeatureJsonWriter(File file, Charset encoding) throws IOException {
        this.writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), encoding);
        writer.write("{\"type\":\"FeatureCollection\",\"features\":[");
    }

    public void write(@NonNull SimpleFeature feature) throws IOException {
        if (first) first = false;
        else writer.write(",");

        if(feature != CACHE_OBJECT) {
            CACHE_STRING = Constants.FEATURE_JSON.toString(feature);
            CACHE_OBJECT = feature;
        }
        writer.write(CACHE_STRING);
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.write("]}");
        writer.close();
    }
}
