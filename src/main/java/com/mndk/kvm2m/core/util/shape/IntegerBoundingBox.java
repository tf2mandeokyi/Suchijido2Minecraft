package com.mndk.kvm2m.core.util.shape;

import com.sk89q.worldedit.regions.FlatRegion;

public class IntegerBoundingBox {
	
	public final int xmin, zmin, xmax, zmax;
	
	public IntegerBoundingBox(int xmin, int zmin, int xmax, int zmax) {
		this.xmin = xmin; this.zmin = zmin; this.xmax = xmax; this.zmax = zmax;
	}
	
	public IntegerBoundingBox(FlatRegion region) {
		this.xmin = (int) Math.floor(region.getMinimumPoint().getX()); this.zmin = (int) Math.floor(region.getMinimumPoint().getZ());
		this.xmax = (int)  Math.ceil(region.getMaximumPoint().getX()); this.zmax = (int)  Math.ceil(region.getMaximumPoint().getZ());
	}
	
	public IntegerBoundingBox getIntersectionArea(IntegerBoundingBox other) {
		int xmin = Math.max(this.xmin, other.xmin), zmin = Math.max(this.zmin, other.zmin);
		int xmax = Math.min(this.xmax, other.xmax), zmax = Math.min(this.zmax, other.zmax);
		
		return new IntegerBoundingBox(xmin, zmin, xmax, zmax);
	}
	
	public boolean isValid() {
		return xmin <= xmax && zmin <= zmax;
	}
}
