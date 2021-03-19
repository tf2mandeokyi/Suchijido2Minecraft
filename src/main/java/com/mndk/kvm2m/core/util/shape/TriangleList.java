package com.mndk.kvm2m.core.util.shape;

import java.util.ArrayList;

import com.mndk.kvm2m.core.util.math.Vector2DH;

@SuppressWarnings("serial")
public class TriangleList extends ArrayList<Triangle> {
	public double interpolateHeight(Vector2DH point) {
		for(Triangle triangle : this) {
			if(triangle.contains(point) != null) {
				return triangle.interpolateY(point);
			}
		}
		return Double.NaN;
	}
}
