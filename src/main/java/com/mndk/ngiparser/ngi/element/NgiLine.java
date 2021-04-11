package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.vertex.NgiVectorList;

public class NgiLine extends NgiRecord<NgiLine.Attr> {
	
	public NgiLine(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVectorList lineData;
	
	public static class Attr implements NgiRecord.Attr {
		public String type;
		public int thickness;
		public int color;
		
		public void from(String[] args) {
			this.type = args[0];
			this.thickness = Integer.parseInt(args[1]);
			this.color = Integer.parseInt(args[2]);
		}
	}
}
