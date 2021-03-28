package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.vertex.NgiVertexList;

public class NgiPolygonElement extends NgiElement<NgiPolygonElement.Attr> {
	
	public NgiPolygonElement(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVertexList[] vertexData;
	
	public static class Attr implements NgiElement.Attr {
		public String lineType;
		public int thickness;
		public int lineColor;
		public String fillType;
		public int color1, color2; // TODO figure out what 2 of these color variables represent
		
		public void from(String[] args) {
			this.lineType = args[0];
			this.thickness = Integer.parseInt(args[1]);
			this.lineColor = Integer.parseInt(args[2]);
			this.fillType = args[3];
			this.color1 = Integer.parseInt(args[4]);
			this.color2 = Integer.parseInt(args[5]);
		}
	}
}
