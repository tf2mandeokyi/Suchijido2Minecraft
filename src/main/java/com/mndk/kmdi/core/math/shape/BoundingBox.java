package com.mndk.kmdi.core.math.shape;

import com.mndk.kmdi.core.math.VectorMath;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;

public class BoundingBox {
	public double x, z, w, h;
	public BoundingBox(double x, double z, double w, double h) {
		this.x = x; this.z = z; this.w = w; this.h = h;
	}
	public BoundingBox(CuboidRegion region) {
		this.x = region.getMinimumPoint().getX(); this.z = region.getMinimumPoint().getZ();
		this.w = region.getWidth(); this.h = region.getHeight();
	}
	public boolean containsPoint(Vector2D point) {
		return x <= point.getX() && point.getX() <= (x+w) &&
				z <= point.getZ() && point.getZ() <= (z+h);
	}
	public boolean checkLineIntersection(Vector2D p0, Vector2D p1) {
		Vector2D dp = p1.subtract(p0);
		return VectorMath.getLineIntersection(p0, dp, new Vector2D(x, z), new Vector2D(w, 0)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2D(x, z), new Vector2D(0, h)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2D(x, z+h), new Vector2D(w, 0)) != null ||
			   VectorMath.getLineIntersection(p0, dp, new Vector2D(x+w, z), new Vector2D(0, h)) != null;
	}
	/*public static void main(String[] args) {
		System.out.println(
				new BoundingBox(-3, -3, 6, 6).checkLineIntersection(new Vector2D(0, 0), new Vector2D(3, 4))
		);
	}*/
}
