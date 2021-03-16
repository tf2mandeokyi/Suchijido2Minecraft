package com.mndk.kmdi.core.util.math;

public class Vector2dH {
	
	public double x, height, z;
	
	public Vector2dH(double x, double height, double z) {
		this.x = x; this.height = height; this.z = z;
	}
	
	public Vector2dH(double x, double z) {
		this.x = x; this.height = 0; this.z = z;
	}
	
	public Vector2dH(com.sk89q.worldedit.Vector v) {
		this.x = v.getX(); this.height = v.getY(); this.z = v.getZ();
	}
	
	public com.sk89q.worldedit.Vector toWorldEditVector() {
		return new com.sk89q.worldedit.Vector(x, height, z);
	}
	
	@Override
    public String toString() {
        return "(" + x + ", " + height + ", " + z + ")";
    }
	
}
