package com.mndk.scjdmc.util.math;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Vector2DH {

	public double x, height, z;
	
	public Vector2DH(double x, double height, double z) {
		this.x = x; this.height = height; this.z = z;
	}
	
	public Vector2DH(double x, double z) {
		this.x = x; this.height = 0; this.z = z;
	}
	
	public Vector2DH add2d(Vector2DH v) {
		return new Vector2DH(x + v.x, z + v.z);
	}
	
	public Vector2DH sub2d(Vector2DH v) {
		return new Vector2DH(x - v.x, z + v.z);
	}
	
	public Vector2DH mult2d(double s) {
		return new Vector2DH(x * s, height, z * s);
	}
	
	public Vector2DH div2d(double s) {
		return new Vector2DH(x / s, height, z / s);
	}
	
	public double dot2d(Vector2DH v) {
		return x * v.x + z * v.z;
	}
	
	public double cross2d(Vector2DH v) {
		return x*v.z - v.x*z;
	}
	
	public double distance2dSq(Vector2DH v) {
		double dx = x - v.x, dz = z - v.z;
		return dx * dx + dz * dz;
	}

	public double distance2d(Vector2DH v) {
		return Math.sqrt(distance2dSq(v));
	}
	
	public Vector2DH withHeight(double height_) {
		return new Vector2DH(x, height_, z);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + height + ", " + z + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Vector2DH)) return false;
		Vector2DH v = (Vector2DH) obj;
		return x == v.x && height == v.height && z == v.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, height, z);
	}

	public boolean equalsXZ(Vector2DH v) {
		return x == v.x && z == v.z;
	}

	public double[] toRoundDoubleArray() {
		return new double[] {
				Math.round(x * 100000000) / 100000000.,
				Math.round(z * 100000000) / 100000000.
		};
	}

	public List<Double> toRoundDoubleList() {
		return Arrays.asList(
				Math.round(x * 100000000) / 100000000.,
				Math.round(z * 100000000) / 100000000.
		);
	}
	
}
