package com.mndk.ngiparser.ngi;

@SuppressWarnings("serial")
public class NgiSyntaxErrorException extends RuntimeException {
    public NgiSyntaxErrorException(String s) {
        super("Error on parsing .ngi file : " + s);
    }
    public NgiSyntaxErrorException() {
        super("Error on parsing .ngi file");
    }
}
