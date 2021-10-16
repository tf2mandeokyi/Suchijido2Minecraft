package com.mndk.scjd2mc.core.util.shape;

import com.sk89q.worldedit.regions.FlatRegion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoundingBoxInteger {
	
	public final int xmin, zmin, xmax, zmax;
	
	public BoundingBoxInteger(FlatRegion region) {
		this.xmin = (int) Math.floor(region.getMinimumPoint().getX()); this.zmin = (int) Math.floor(region.getMinimumPoint().getZ());
		this.xmax = (int)  Math.ceil(region.getMaximumPoint().getX()); this.zmax = (int)  Math.ceil(region.getMaximumPoint().getZ());
	}
	
	public BoundingBoxInteger and(BoundingBoxInteger other) {
		int xmin = Math.max(this.xmin, other.xmin), zmin = Math.max(this.zmin, other.zmin);
		int xmax = Math.min(this.xmax, other.xmax), zmax = Math.min(this.zmax, other.zmax);
		
		return new BoundingBoxInteger(xmin, zmin, xmax, zmax);
	}

	public BoundingBoxInteger or(BoundingBoxInteger other) {
		int xmin = Math.min(this.xmin, other.xmin), zmin = Math.min(this.zmin, other.zmin);
		int xmax = Math.max(this.xmax, other.xmax), zmax = Math.max(this.zmax, other.zmax);

		return new BoundingBoxInteger(xmin, zmin, xmax, zmax);
	}

	public boolean isPointInside(int x, int z) {
		return x >= xmin && x <= xmax && z >= zmin && z <= zmax;
	}
	
	public boolean isValid() {
		return xmin <= xmax && zmin <= zmax;
	}
}