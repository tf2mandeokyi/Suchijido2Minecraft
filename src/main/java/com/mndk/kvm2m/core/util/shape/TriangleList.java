package com.mndk.kvm2m.core.util.shape;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class TriangleList extends ArrayList<Triangle> {
	public double interpolateHeight(int x, int z) {
		for(Triangle triangle : this) {
			if(triangle.contains(x + .5, z + .5) != null) {
				return triangle.interpolateY(x + .5, z + .5);
			}
		}
		return Double.NaN;
	}

	public double interpolateHeight(double x, double z) {
		return interpolateHeight((int) Math.floor(x), (int) Math.floor(z));
	}
}
