package com.mndk.scjdmc.ngiparser.ngi.vertex;

import java.util.Arrays;

public class NgiVectorList {
	
	final NgiVector[] list;
	
	public NgiVectorList(NgiVector[] list) {
		this.list = list;
	}
	
	public int getSize() {
		return list.length;
	}
	
	public NgiVector getVertex(int n) {
		return list[n];
	}
	
	@Override
	public String toString() {
		return Arrays.deepToString(list);
	}
	
}
