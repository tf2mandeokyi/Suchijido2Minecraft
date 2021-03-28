package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.vertex.NgiVertex;

public class NgiTextElement extends NgiElement<NgiTextElement.Attr> {
	
	public NgiTextElement(NgiLayer parent) {
		super(parent);
	}
	
	public String text;
	public NgiVertex position;
	
	public static class Attr implements NgiElement.Attr {
		public String fontName;
		public double fontSize;
		public int color;
		public double unknownFloat;
		public int unknownInt;
		
		public void from(String[] args) {
			this.fontName = args[0];
			this.fontSize = Double.parseDouble(args[1]);
			this.color = Integer.parseInt(args[2]);
			this.unknownFloat = Double.parseDouble(args[2]);
			this.unknownInt = Integer.parseInt(args[2]);
		}
	}
}
