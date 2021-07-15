package com.mndk.kvm2m.core.util.shape;

import com.sk89q.worldedit.regions.FlatRegion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoundingBoxInteger {
	
	public final int xmin, zmin, xmax, zmax;
	
	public BoundingBoxInteger(FlatRegion region) {
		this.xmin = (int) Math.floor(region.getMinimumPoint().getX()); this.zmin = (int) Math.floor(region.getMinimumPoint().getZ());
		this.xmax = (int)  Math.ceil(region.getMaximumPoint().getX()); this.zmax = (int)  Math.ceil(region.getMaximumPoint().getZ());
	}
	
	public BoundingBoxInteger getIntersectionArea(BoundingBoxInteger other) {
		int xmin = Math.max(this.xmin, other.xmin), zmin = Math.max(this.zmin, other.zmin);
		int xmax = Math.min(this.xmax, other.xmax), zmax = Math.min(this.zmax, other.zmax);
		
		return new BoundingBoxInteger(xmin, zmin, xmax, zmax);
	}
	
	public boolean isValid() {
		return xmin <= xmax && zmin <= zmax;
	}
}
