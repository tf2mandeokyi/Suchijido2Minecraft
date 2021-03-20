package com.mndk.kvm2m.core.util.shape;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.sk89q.worldedit.regions.CuboidRegion;

public class BoundingBox {
	
	
	
	public double xmin, zmin, xmax, zmax;
	
	
	
	public BoundingBox(double xmin, double zmin, double xmax, double zmax) {
		this.xmin = xmin; this.zmin = zmin; this.xmax = xmax; this.zmax = zmax;
	}
	
	
	
	public BoundingBox(CuboidRegion region) {
		this.xmin = region.getMinimumPoint().getX(); this.zmin = region.getMinimumPoint().getZ();
		this.xmax = region.getMaximumPoint().getX(); this.zmax = region.getMaximumPoint().getZ();
	}
	
	
	
	public boolean containsPoint(Vector2DH point) {
		return xmin <= point.x && point.x <= xmax &&
			   zmin <= point.z && point.z <= zmax;
	}
	
	
	
	/*public boolean checkLineIntersection(Vector2DH p0, Vector2DH p1) {
		Vector2DH dp = p1.sub2d(p0);
		return VectorMath.getLineIntersection(p0, dp, new Vector2DH(xmin, zmin), new Vector2DH(w, 0)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2DH(x, z), new Vector2DH(0, h)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2DH(x, z+h), new Vector2DH(w, 0)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2DH(x+w, z), new Vector2DH(0, h)) != null;
	}
	
	
	
	public boolean checkLineInside(Vector2DH p0, Vector2DH p1) {
		return this.containsPoint(p0) || this.containsPoint(p1) || checkLineIntersection(p0, p1);
	}*/
}
