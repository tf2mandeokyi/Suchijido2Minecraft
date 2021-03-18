package com.mndk.ngiparser.ngi;

@SuppressWarnings("serial")
public class NgiParseException extends RuntimeException {
    public NgiParseException(String s) {
        super("Error on parsing .ngi file : " + s);
    }
    public NgiParseException() {
        super("Error on parsing .ngi file");
    }
}
