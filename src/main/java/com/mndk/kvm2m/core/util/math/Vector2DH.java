package com.mndk.kvm2m.core.util.math;

import com.google.gson.JsonArray;

import java.util.Objects;

public class Vector2DH {

	public double x, height, z;
	
	public Vector2DH(double x, double height, double z) {
		this.x = x; this.height = height; this.z = z;
	}
	
	public Vector2DH(double x, double z) {
		this.x = x; this.height = 0; this.z = z;
	}
	
	public Vector2DH(com.sk89q.worldedit.Vector v) {
		this.x = v.getX(); this.height = v.getY(); this.z = v.getZ();
	}
	
	public Vector2DH(com.sk89q.worldedit.Vector2D v) {
		this.x = v.getX(); this.height = 0; this.z = v.getZ();
	}
	
	public com.sk89q.worldedit.Vector toWorldEditVector() {
		return new com.sk89q.worldedit.Vector(x, height, z);
	}
	
	public com.sk89q.worldedit.Vector toIntegerWorldEditVector() {
		return new com.sk89q.worldedit.Vector((int) x, (int) height, (int) z);
	}
	
	public com.sk89q.worldedit.Vector toWorldEditVector(double y) {
		return new com.sk89q.worldedit.Vector(x, y, z);
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

	public JsonArray toJsonArray() {
		JsonArray array = new JsonArray();
		array.add(Math.round(x * 100000000) / 100000000.);
		array.add(Math.round(z * 100000000) / 100000000.);
		return array;
	}
	
}
