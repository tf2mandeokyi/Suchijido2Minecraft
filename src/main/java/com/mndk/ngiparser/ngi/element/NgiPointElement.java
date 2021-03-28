package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.vertex.NgiVertex;

public class NgiPointElement extends NgiElement<NgiPointElement.Attr> {
	
	public NgiPointElement(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVertex position;
	
	public static class Attr implements NgiElement.Attr {
		public String type;
		public double unknownFloat;
		public int color;
		
		public void from(String[] args) {
			this.type = args[0];
			this.unknownFloat = Double.parseDouble(args[1]);
			this.color = Integer.parseInt(args[2]);
		}
	}
}
