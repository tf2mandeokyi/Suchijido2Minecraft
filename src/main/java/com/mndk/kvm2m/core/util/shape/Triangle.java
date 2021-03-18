package com.mndk.kvm2m.core.util.shape;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class Triangle {
	
	
	
	public final Vector2DH v1, v2, v3;

	
	
	public Triangle(Vector2DH a, Vector2DH b, Vector2DH c) {
		this.v1 = a; this.v2 = b; this.v3 = c;
	}
	
	
	
	public Vector contains(Vector2D point) {
        return contains(point.toVector());
    }
	
	public Vector contains(Vector p) {
		/* Bounding box test first, for quick rejections. */
		if ((p.getX() < v1.x && p.getX() < v2.x && p.getX() < v3.x) ||
			(p.getX() > v1.x && p.getX() > v2.x && p.getX() > v3.x) ||
			(p.getZ() < v1.z && p.getZ() < v2.z && p.getZ() < v3.z) ||
			(p.getZ() > v1.z && p.getZ() > v2.z && p.getZ() > v3.z))
			
			return null;

		double a = v2.x - v1.x,
			   b = v3.x - v1.x,
			   c = v2.z - v1.z,
			   d = v3.z - v1.z,
			   i = a * d - b * c;

		/* Degenerate tri. */
		if(i == 0.0)
			return null;

		double u = (d * (p.getX() - v1.x) - b * (p.getZ() - v1.z)) / i,
			   v = (a * (p.getZ() - v1.z) - c * (p.getX() - v1.x)) / i;

		/* If we're outside the tri, fail. */
		if(u < 0.0 || v < 0.0 || (u + v) > 1.0)
			return null;

		return new Vector(u, 0, v);
	}
	
	
	
	public boolean contains_line(Vector2DH point) {
		double d1, d2, d3;
		boolean has_neg, has_pos;
		
		d1 = sign(point, v1, v2);
		d2 = sign(point, v2, v3);
		d3 = sign(point, v3, v1);
		
		has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
	    has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

	    return !(has_neg && has_pos);
    }
	
	private static double sign(Vector2DH p1, Vector2DH p2, Vector2DH p3) {
		return (p1.x - p3.x) * (p2.z - p3.z) - (p2.x - p3.x) * (p1.z - p3.z);
	}
	
	
	
	public boolean isPointInCircumcircle(Vector point) {
		double a11 = v1.x - point.getX();
        double a21 = v2.x - point.getX();
        double a31 = v3.x - point.getX();

        double a12 = v1.z - point.getZ();
        double a22 = v2.z - point.getZ();
        double a32 = v3.z - point.getZ();

        double a13 = (v1.x - point.getX()) * (v1.x - point.getX()) + (v1.z - point.getZ()) * (v1.z - point.getZ());
        double a23 = (v2.x - point.getX()) * (v2.x - point.getX()) + (v2.z - point.getZ()) * (v2.z - point.getZ());
        double a33 = (v3.x - point.getX()) * (v3.x - point.getX()) + (v3.z - point.getZ()) * (v3.z - point.getZ());

        double det = a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a13 * a22 * a31 - a12 * a21 * a33 - a11 * a23 * a32;
        
        return this.isOrientedCCW() ? det > 0.0d : det < 0.0d;
	}
	
	
	
	public boolean isOrientedCCW() {
        double a11 = v1.x - v3.x;
        double a21 = v2.x - v3.x;

        double a12 = v1.z - v3.z;
        double a22 = v2.z - v3.z;

        double det = a11 * a22 - a12 * a21;

        return det > 0.0d;
    }
	
	
	
	public double interpolateY(Vector2DH point) {
		double x_1 = v1.x, x_2 = v2.x, x_3 = v3.x;
		double y_1 = v1.z, y_2 = v2.z, y_3 = v3.z;
		double p_x = point.x, p_y = point.z;
		
		double denom = (y_2 - y_3) * (x_1 - x_3) + (x_3 - x_2) * (y_1 - y_3);
		double w_1 = ( (y_2 - y_3) * (p_x - x_3) + (x_3 - x_2) * (p_y - y_3) ) / denom;
		double w_2 = ( (y_3 - y_1) * (p_x - x_3) + (x_1 - x_3) * (p_y - y_3) ) / denom;
		double w_3 = 1 - w_1 - w_2;
		
		return v1.height * w_1 + v2.height * w_2 + v3.height * w_3;
	}
	
	
	
	@Override
    public String toString() {
        return "Triangle[" + v1 + ", " + v2 + ", " + v3 + "]";
    }
	
}
