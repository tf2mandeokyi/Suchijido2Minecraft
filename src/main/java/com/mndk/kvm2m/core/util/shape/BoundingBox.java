package com.mndk.kvm2m.core.util.shape;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.math.VectorMath;
import com.sk89q.worldedit.regions.CuboidRegion;

public class BoundingBox {
	
	
	
	public double x, z, w, h;
	
	
	
	public BoundingBox(double x, double z, double w, double h) {
		this.x = x; this.z = z; this.w = w; this.h = h;
	}
	
	
	
	public BoundingBox(CuboidRegion region) {
		this.x = region.getMinimumPoint().getX(); this.z = region.getMinimumPoint().getZ();
		this.w = region.getWidth(); this.h = region.getLength();
	}
	
	
	
	public boolean containsPoint(Vector2DH point) {
		return x <= point.x && point.x <= (x+w) &&
				z <= point.z && point.z <= (z+h);
	}
	
	
	
	public boolean checkLineIntersection(Vector2DH p0, Vector2DH p1) {
		Vector2DH dp = p1.sub2d(p0);
		return VectorMath.getLineIntersection(p0, dp, new Vector2DH(x, z), new Vector2DH(w, 0)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2DH(x, z), new Vector2DH(0, h)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2DH(x, z+h), new Vector2DH(w, 0)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2DH(x+w, z), new Vector2DH(0, h)) != null;
	}
	
	
	
	public boolean checkLineInside(Vector2DH p0, Vector2DH p1) {
		return this.containsPoint(p0) || this.containsPoint(p1) || checkLineIntersection(p0, p1);
	}
}
