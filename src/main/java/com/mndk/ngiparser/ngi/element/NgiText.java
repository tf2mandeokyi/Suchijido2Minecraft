package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.vertex.NgiVector;

public class NgiText extends NgiRecord<NgiText.Attr> {
	
	public NgiText(NgiLayer parent) {
		super(parent);
	}
	
	public String text;
	public NgiVector position;
	
	public static class Attr implements NgiRecord.Attr {
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
