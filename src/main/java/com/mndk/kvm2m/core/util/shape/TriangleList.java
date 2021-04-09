package com.mndk.kvm2m.core.util.shape;

import java.util.ArrayList;

import com.mndk.kvm2m.core.util.math.Vector2DH;

@SuppressWarnings("serial")
public class TriangleList extends ArrayList<Triangle> {
	public double interpolateHeight(int x, int z) {
		Vector2DH point = new Vector2DH(x, z);
		for(Triangle triangle : this) {
			if(triangle.contains(point) != null) {
				return triangle.interpolateY(point);
			}
		}
		return Double.NaN;
	}
}
