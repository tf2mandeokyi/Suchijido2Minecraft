package com.mndk.scjdmc.util.io;

import com.mndk.scjdmc.util.math.Vector2DH;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class SimpleVertexEdgeWriter implements Closeable {

    public static final byte EDGE = 1, VERTEX = 2, END = 0;

    private final OutputStream writer;

    public SimpleVertexEdgeWriter(File file) throws IOException {
        this.writer = Files.newOutputStream(file.toPath());
    }

    public void writeEdge(Vector2DH v1, Vector2DH v2) throws IOException {
        if(v2 == null) this.writeVertex(v1);
        else synchronized (this) {
            writer.write(EDGE);
            writer.write(v1.toByteArray());
            writer.write(v2.toByteArray());
        }
    }

    public void writeVertex(Vector2DH v) throws IOException {
        synchronized (this) {
            writer.write(VERTEX);
            writer.write(v.toByteArray());
        }
    }

    @Override
    public void close() throws IOException {
        writer.write(END);
        writer.close();
    }
}
