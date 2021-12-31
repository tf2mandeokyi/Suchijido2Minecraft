package com.mndk.scjdmc.util.math;

public class VectorMath {

	public static Vector2DH getLineStraightIntersection(Vector2DH sLineP0, Vector2DH sLinePDelta, Vector2DH lineStart, Vector2DH lineEnd) {
		Vector2DH lineDelta = lineEnd.sub2d(lineStart);
		
		double u = sLineP0.sub2d(lineStart).cross2d(sLinePDelta.div2d(lineDelta.cross2d(sLinePDelta)));
		
		if(0 <= u && u < 1) return lineStart.add2d(lineDelta.mult2d(u));
		
		return null;
	}

	public static Vector2DH getLineRayIntersection(Vector2DH rayStart, Vector2DH rayDelta, Vector2DH lineStart, Vector2DH lineEnd) {
		Vector2DH lineDelta = lineEnd.sub2d(lineStart);
		
		double t = lineStart.sub2d(rayStart).cross2d(lineDelta.div2d(rayDelta.cross2d(lineDelta)));
		double u = rayStart.sub2d(lineStart).cross2d(rayDelta.div2d(lineDelta.cross2d(rayDelta)));
		
		if(0 <= t && 0 <= u && u < 1) return rayStart.add2d(rayDelta.mult2d(t));
		
		return null;
	}

	public static boolean checkRayXIntersection(Vector2DH point, Vector2DH p0, Vector2DH p1) {
		return ((p0.z > point.z) != (p1.z > point.z)) &&
				(point.x < (p1.x - p0.x) * (point.z - p0.z) / (p1.z - p0.z) + p0.x);
	}

	public static Vector2DH getLineIntersection(Vector2DH line0Start, Vector2DH line0Delta, Vector2DH line1Start, Vector2DH line1Delta) {
		// r0 + t * rayDelta = l0 + u * lineDelta
		// 0 <= t <= 1 && 0 <= u <= 1
		
		double t = line1Start.sub2d(line0Start).cross2d(line1Delta.div2d(line0Delta.cross2d(line1Delta)));
		double u = line0Start.sub2d(line1Start).cross2d(line0Delta.div2d(line1Delta.cross2d(line0Delta)));
		
		if(0 <= t && t < 1 && 0 <= u && u < 1) return line0Start.add2d(line0Delta.mult2d(t));
		
		return null;
	}
}