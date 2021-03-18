package com.mndk.ngiparser.ngi.element;

public class NgiLineElement extends NgiElement<NgiLineElement.Attr> {
	
	public double[][] lineData;
	
    public static class Attr extends NgiElement.Attr {
    	public String type;
    	public int thickness;
    	public int color;
    }
}
