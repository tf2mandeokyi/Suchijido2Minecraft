package com.mndk.ngiparser.ngi.vertex;

import scala.actors.threadpool.Arrays;

public class NgiVertexList {
	
	final NgiVertex[] list;
	
	public NgiVertexList(NgiVertex[] list) {
		this.list = list;
	}
	
	public int getSize() {
		return list.length;
	}
	
	public NgiVertex getVertex(int n) {
		return list[n];
	}
	
	@Override
	public String toString() {
		return Arrays.deepToString(list);
	}
	
}
