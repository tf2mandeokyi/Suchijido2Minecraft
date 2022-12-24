package com.mndk.scjdmc.util.math;

import org.locationtech.jts.geom.Coordinate;

import java.util.Objects;

public class Vector2DH {

	public double x, height, z;

	public Vector2DH(double x, double height, double z) {
		this.x = x; this.height = height; this.z = z;
	}
	
	public Vector2DH(double x, double z) {
		this(x, 0, z);
	}

	public Vector2DH(Coordinate coordinate, double height) {
		this(coordinate.x, height, coordinate.y);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + height + ", " + z + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Vector2DH v)) return false;
		return x == v.x && height == v.height && z == v.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, height, z);
	}

	public boolean equalsXZ(Vector2DH v) {
		return x == v.x && z == v.z;
	}
	
}
