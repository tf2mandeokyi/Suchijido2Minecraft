package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.vertex.NgiVector;

public class NgiPoint extends NgiRecord<NgiPoint.Attr> {
	
	public NgiPoint(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVector position;
	
	public static class Attr implements NgiRecord.Attr {
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
