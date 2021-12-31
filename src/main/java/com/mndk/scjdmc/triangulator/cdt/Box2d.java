package com.mndk.scjdmc.triangulator.cdt;

import com.mndk.scjdmc.util.math.Vector2DH;

import java.util.List;


/**
 * 2D bounding box.
 * @author artem-ogre
 */
public class Box2d {
	
	public Vector2DH min, max;
	
	public Box2d(Vector2DH min, Vector2DH max) {
		this.min = min; this.max = max;
	}
	
	/**
	 * Bounding box of a collection of 2D points.
	 */
	public static Box2d envelope(List<Vector2DH> vertices) {
		double max = Double.MAX_VALUE;
		Box2d box = new Box2d(new Vector2DH(max, max), new Vector2DH(-max, -max));
		for(Vector2DH vertex : vertices) {
			box.min.x = Math.min(vertex.x, box.min.x);
			box.max.x = Math.max(vertex.x, box.max.x);
			box.min.z = Math.min(vertex.z, box.min.z);
			box.max.z = Math.max(vertex.z, box.max.z);
		}
		return box;
	}
	
}
