package com.mndk.ngiparser.ngi.vertex;

import scala.actors.threadpool.Arrays;

public class NgiVertex {

	final double[] array;
	
	public NgiVertex(double[] array) {
		this.array = array;
	}
	
	public int getDimensions() {
		return array.length;
	}
	
	public double getAxis(int n) {
		return array[n];
	}
	
	@Override
	public String toString() {
		return Arrays.toString(array);
	}
	
}
