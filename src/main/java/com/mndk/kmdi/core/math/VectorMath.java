package com.mndk.kmdi.core.math;

import javax.annotation.Nullable;

import com.sk89q.worldedit.Vector2D;

public class VectorMath {
	@Nullable
	public static Vector2D getLineStraightIntersection(Vector2D sLineP0, Vector2D sLinePDelta, Vector2D lineStart, Vector2D lineEnd) {
		Vector2D lineDelta = lineEnd.subtract(lineStart);
		
		double u = cross(sLineP0.subtract(lineStart), sLinePDelta.divide(cross(lineDelta, sLinePDelta)));
		
		if(0 <= u && u < 1) return lineStart.add(lineDelta.multiply(u));
		
		return null;
	}

	@Nullable
	public static Vector2D getLineRayIntersection(Vector2D rayStart, Vector2D rayDelta, Vector2D lineStart, Vector2D lineEnd) {
		Vector2D lineDelta = lineEnd.subtract(lineStart);
		
		double t = cross(lineStart.subtract(rayStart), lineDelta.divide(cross(rayDelta, lineDelta)));
		double u = cross(rayStart.subtract(lineStart), rayDelta.divide(cross(lineDelta, rayDelta)));
		
		if(0 <= t && 0 <= u && u < 1) return rayStart.add(rayDelta.multiply(t));
		
		return null;
	}

	@Nullable
	public static Vector2D getLineIntersection(Vector2D line0Start, Vector2D line0Delta, Vector2D line1Start, Vector2D line1Delta) {
		// r0 + t * rayDelta = l0 + u * lineDelta
		// 0 <= t <= 1 && 0 <= u <= 1
		
		double t = cross(line1Start.subtract(line0Start), line1Delta.divide(cross(line0Delta, line1Delta)));
		double u = cross(line0Start.subtract(line1Start), line0Delta.divide(cross(line1Delta, line0Delta)));
		
		if(0 <= t && t < 1 && 0 <= u && u < 1) return line0Start.add(line0Delta.multiply(t));
		
		return null;
	}
	
	public static double cross(Vector2D v1, Vector2D v2) {
    	return v1.getX()*v2.getZ() - v2.getX()*v1.getZ();
    }
	
	public static Vector2D getClosestPointToLine(Vector2D p, Vector2D l0, Vector2D l1) {
		
		if(l0.getX() == l1.getX() && l0.getZ() == l1.getZ()) return l0;
		
		Vector2D v = new Vector2D(l1.getX() - l0.getX(), l1.getZ() - l0.getZ());
		Vector2D w = new Vector2D(p.getX() - l0.getX(), p.getZ() - l0.getZ());
		
		double c1 = w.dot(v);
		if(c1 <= 0) return l0;
		
		double c2 = v.dot(v);
		if(c2 <= c1) return l1;
		
		double b = c1 / c2;
		return new Vector2D(l0.getX() + b * v.getX(), l0.getZ() + b * v.getZ());
	}
}