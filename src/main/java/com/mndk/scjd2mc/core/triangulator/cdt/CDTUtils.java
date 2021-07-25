package com.mndk.scjd2mc.core.triangulator.cdt;

import com.mndk.scjd2mc.core.util.math.Vector2DH;

import java.util.List;

public class CDTUtils {

	
	
	/**
	 * Advance vertex or neighbor index counter-clockwise
	 */
	public static int ccw(int i) {
		return (i + 1) % 3;
	}
	
	
	
	/**
	 * Advance vertex or neighbor index clockwise
	 */
	public static int cw(int i) {
		return (i + 2) % 3;
	}
	
	
	
	/**
	 * Neighbor index from a on-edge location
	 * Note: Call only if located on the edge!
	 */
	public static int edgeNeighbor(final PtTriLocation location) {
		int result = location.ordinal() - PtTriLocation.ON_EDGE1.ordinal();
		assert result >= 0;
		return result;
	}
	
	
	
	/**
	 * Test if point lies in a circumscribed circle of a triangle.
	 */
	public static boolean inCircumCircle(
			final Vector2DH p,
			final Vector2DH v1,
			final Vector2DH v2,
			final Vector2DH v3
	) {
		double dx = v1.x - p.x;
		double dy = v1.z - p.z;
		double ex = v2.x - p.x;
		double ey = v2.z - p.z;
		double fx = v3.x - p.x;
		double fy = v3.z - p.z;
		
		return (
		        (dx*dx + dy*dy) * (ex*fy - fx*ey) -
		        (ex*ex + ey*ey) * (dx*fy - fx*dy) +
		        (fx*fx + fy*fy) * (dx*ey - ex*dy)
		    ) > 0;
	}
	
	
	
	/**
	 * Check if location is classified as on any of three edges
	 */
	public static boolean onEdge(PtTriLocation location) {
		return location.ordinal() > PtTriLocation.OUTSIDE.ordinal();
	}
	
	
	
	/**
	 * Check if point lies to the left of, to the right of, or on a line.
	 */
	public static PtLineLocation locatePointLine(Vector2DH p, Vector2DH v1, Vector2DH v2) {
		
		double dx = v2.x - v1.x;
		double dy = v2.z - v1.z;
		double ex = p.x - v1.x;
		double ey = p.z - v1.z;
		
		double product = dx * ey - dy * ex;
		
		if(product < 0) return PtLineLocation.RIGHT;
		if(product > 0) return PtLineLocation.LEFT;
		return PtLineLocation.ON_LINE;
		
	}
	
	
	
	/**
	 * Check if point a lies inside of, outside of, or on an edge of a triangle.
	 */
	public static PtTriLocation locatePointTriangle(Vector2DH p, Vector2DH v1, Vector2DH v2, Vector2DH v3) {
		PtTriLocation result = PtTriLocation.INSIDE;
		
		PtLineLocation edgeCheck = locatePointLine(p, v1, v2);
		if(edgeCheck == PtLineLocation.RIGHT) {
	        return PtTriLocation.OUTSIDE;
		}
	    if(edgeCheck == PtLineLocation.ON_LINE) {
	        result = PtTriLocation.ON_EDGE1;
	    }
	    
	    edgeCheck = locatePointLine(p, v2, v3);
	    if(edgeCheck == PtLineLocation.RIGHT) {
	        return PtTriLocation.OUTSIDE;
	    }
	    if(edgeCheck == PtLineLocation.ON_LINE) {
	        result = PtTriLocation.ON_EDGE2;
	    }
	    
	    edgeCheck = locatePointLine(p, v3, v1);
	    if(edgeCheck == PtLineLocation.RIGHT) {
	        return PtTriLocation.OUTSIDE;
	    }
	    if(edgeCheck == PtLineLocation.ON_LINE) {
	        result = PtTriLocation.ON_EDGE3;
	    }
	    
	    return result;
	}
	
	
	
	/**
	 * If triangle has a given neighbor return neighbor-index, throw otherwise.
	 */
	public static int neighborIndex(IndexTriangle tri, int iTnbr) {
		if(iTnbr == tri.neighbors.e1) return 0;
		if(iTnbr == tri.neighbors.e2) return 1;
		if(iTnbr == tri.neighbors.e3) return 2;
		throw new RuntimeException("Could not find neighbor triangle index");
	}
	
	
	
	/**
	 * Opposed neighbor index from vertex index.
	 */
	public static int opoNbr(int vertIndex) {
		if(vertIndex >= 0 && vertIndex <= 2) return (vertIndex + 1) % 3;
		throw new RuntimeException("Invalid vertex index");
	}
	
	
	
	/**
	 * Opposed vertex index from neighbor index.
	 */
	public static int opoVrt(int neighborIndex) {
		if(neighborIndex >= 0 && neighborIndex <= 2) return (neighborIndex + 2) % 3;
		throw new RuntimeException("Invalid neighbor index");
	}
	
	
	
	/**
	 * Given triangle and a vertex find opposed triangle.
	 */
	public static int opposedTriangle(IndexTriangle tri, int iVert) {
		return tri.neighbors.get(opposedTriangleIndex(tri, iVert));
	}
	
	
	
	/**
	 * Index of triangle's neighbor opposed to a vertex.
	 */
	public static int opposedTriangleIndex(IndexTriangle tri, int iVert) {
		if(iVert == tri.vertices.e1) return opoNbr(0);
		if(iVert == tri.vertices.e2) return opoNbr(1);
		if(iVert == tri.vertices.e3) return opoNbr(2);
		throw new RuntimeException("Could not find opposed triangle index (triangle: " + tri.vertices + ", vertex: " + iVert + ")");
	}
	
	
	
	/**
	 * Index of triangle's neighbor opposed to an edge.
	 */
	public static int opposedTriangleIndex(IndexTriangle tri, int iVedge1, int iVedge2) {
		if(iVedge1 != tri.vertices.e1 && iVedge2 != tri.vertices.e1) return opoNbr(0);
		if(iVedge1 != tri.vertices.e2 && iVedge2 != tri.vertices.e2) return opoNbr(1);
		if(iVedge1 != tri.vertices.e3 && iVedge2 != tri.vertices.e3) return opoNbr(2);
		throw new RuntimeException("Could not find opposed-to-edge triangle index");
	}
	
	
	
	/**
	 * Given two triangles, return vertex of first triangle opposed to the second.
	 */
	public static int opposedVertex(IndexTriangle tri, int iTopo) {
		return tri.vertices.get(opposedVertexIndex(tri, iTopo));
	}
	
	
	
	/**
	 * Index of triangle's vertex opposed to a triangle
	 */
	public static int opposedVertexIndex(IndexTriangle tri, int iTopo) {
		if(iTopo == tri.neighbors.e1) return opoVrt(0);
		if(iTopo == tri.neighbors.e2) return opoVrt(1);
		if(iTopo == tri.neighbors.e3) return opoVrt(2);
		throw new RuntimeException("Could not find opposed vertex index");
	}
	
	
	
	/**
	 * If triangle has a given vertex return vertex-index, throw otherwise.
	 */
	public static int vertexIndex(IndexTriangle tri, int iV) {
		if(iV == tri.vertices.e1) return 0;
		if(iV == tri.vertices.e2) return 1;
		if(iV == tri.vertices.e3) return 2;
		throw new RuntimeException("Could not find vertex index in triangle");
	}
	
	
	
	/**
	 * Test if two vertices share at least one common triangle.
	 */
	public static boolean verticesShareEdge(Vertex a, Vertex b) {
		List<Integer> aTris = a.triangles, bTris = b.triangles;
		for(int it : aTris) {
			if(bTris.contains(it)) {
				return true;
			}
		}
		return false;
	}
}
