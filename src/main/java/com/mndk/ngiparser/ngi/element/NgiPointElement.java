package com.mndk.ngiparser.ngi.element;

public class NgiPointElement extends NgiElement<NgiPointElement.Attr> {
	
	public double[] position;
	
    public static class Attr extends NgiElement.Attr {
        public String type;
        public double unknownFloat;
        public int color;
    }
}
