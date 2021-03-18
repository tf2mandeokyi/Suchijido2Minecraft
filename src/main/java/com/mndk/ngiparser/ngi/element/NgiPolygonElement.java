package com.mndk.ngiparser.ngi.element;

public class NgiPolygonElement extends NgiElement<NgiPolygonElement.Attr> {
	
	public double[][][] vertexData;
	
    public static class Attr extends NgiElement.Attr {
    	public String lineType;
    	public int thickness;
    	public int lineColor;
    	public String fillType;
    	public int color1, color2;
    }
}
