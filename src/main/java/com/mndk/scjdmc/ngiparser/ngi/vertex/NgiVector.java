package com.mndk.scjdmc.ngiparser.ngi.vertex;

import java.util.Arrays;

public class NgiVector {

	final double[] array;
	
	public NgiVector(double[] array) {
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
