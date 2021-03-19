package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.vertex.NgiVertexList;

public class NgiLineElement extends NgiElement<NgiLineElement.Attr> {
	
	public NgiLineElement(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVertexList lineData;
	
    public static class Attr implements NgiElement.Attr {
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
