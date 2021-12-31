package com.mndk.scjdmc.ngiparser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

public class DebuggableLineReader extends BufferedReader {

    private int line;
    private final String fileName;
    private String lineCache;

    public DebuggableLineReader(Reader in, String fileName) {
        super(in);
        this.line = 0;
        this.fileName = fileName;
    }

    @Override
    public String readLine() throws IOException {
        this.line++;
        return lineCache = super.readLine();
    }

    public IOException getException() {
        return new IOException(
                "Exception thrown while parsing line " + line + " of file \"" + fileName +
                "\": \"" + lineCache + "\""
        );
    }

    public IOException getException(String message) {
        return new IOException(
                "Exception thrown while parsing line " + line + " of file \"" + fileName + "\": \"" + lineCache + "\"\n" +
                "Cause: " + message
        );
    }

    public IOException getException(Throwable t) {
        return new IOException(
                "Exception thrown while parsing line " + line + " of file \"" + fileName + "\": \"" + lineCache + "\"",
                t
        );
    }

    public int read() {
        throw new UnsupportedOperationException("Only readLine() is supported in DebuggableBufferedLineReader.");
    }

    @Override
    public int read(CharBuffer target) {
        throw new UnsupportedOperationException("Only readLine() is supported in DebuggableBufferedLineReader.");
    }

    @Override
    public int read(char[] cbuf) {
        throw new UnsupportedOperationException("Only readLine() is supported in DebuggableBufferedLineReader.");
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        throw new UnsupportedOperationException("Only readLine() is supported in DebuggableBufferedLineReader.");
    }
}
