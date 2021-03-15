package com.mndk.kmdi.core.util.shape;

import java.util.Arrays;

import com.mndk.kmdi.core.util.math.VectorMath;
import com.mndk.kmdi.core.util.triangulation.Edge;
import com.mndk.kmdi.core.util.triangulation.EdgeWithDistance;
import com.sk89q.worldedit.Vector;

public class Triangle {
	
	
	
	public final Vector a, b, c;

	
	
	public Triangle(Vector a, Vector b, Vector c) {
		this.a = a; this.b = b; this.c = c;
	}
	
	
	
	public boolean contains(Vector point) {
        double pab = VectorMath.cross2d(point.subtract(a), b.subtract(a));
        double pbc = VectorMath.cross2d(point.subtract(b), c.subtract(b));

        if (!hasSameSign(pab, pbc)) {
            return false;
        }

        double pca = VectorMath.cross2d(point.subtract(c), a.subtract(c));

        if (!hasSameSign(pab, pca)) {
            return false;
        }

        return true;
    }
	
	
	
	public boolean isPointInCircumcircle(Vector point) {
		double a11 = a.getX() - point.getX();
        double a21 = b.getX() - point.getX();
        double a31 = c.getX() - point.getX();

        double a12 = a.getZ() - point.getZ();
        double a22 = b.getZ() - point.getZ();
        double a32 = c.getZ() - point.getZ();

        double a13 = (a.getX() - point.getX()) * (a.getX() - point.getX()) + (a.getZ() - point.getZ()) * (a.getZ() - point.getZ());
        double a23 = (b.getX() - point.getX()) * (b.getX() - point.getX()) + (b.getZ() - point.getZ()) * (b.getZ() - point.getZ());
        double a33 = (c.getX() - point.getX()) * (c.getX() - point.getX()) + (c.getZ() - point.getZ()) * (c.getZ() - point.getZ());

        double det = a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a13 * a22 * a31 - a12 * a21 * a33 - a11 * a23 * a32;
        
        return this.isOrientedCCW() ? det > 0.0d : det < 0.0d;
	}
	
	
	
	public boolean isOrientedCCW() {
        double a11 = a.getX() - c.getX();
        double a21 = b.getX() - c.getX();

        double a12 = a.getZ() - c.getZ();
        double a22 = b.getZ() - c.getZ();

        double det = a11 * a22 - a12 * a21;

        return det > 0.0d;
    }
	
	
	
	public boolean isNeighbour(Edge edge) {
        return (a == edge.a || b == edge.a || c == edge.a) && (a == edge.b || b == edge.b || c == edge.b);
    }
	
	
	
	public Vector getNoneEdgeVertex(Edge edge) {
        if (a != edge.a && a != edge.b) return a;
        if (b != edge.a && b != edge.b) return b;
        if (c != edge.a && c != edge.b) return c;

        return null;
    }
	
	
	
	public boolean hasVertex(Vector vertex) {
        return a == vertex || b == vertex || c == vertex;
    }
	
	
	
	public EdgeWithDistance findNearestEdge(Vector point) {
		EdgeWithDistance[] edges = new EdgeWithDistance[3];

        edges[0] = new EdgeWithDistance(new Edge(a, b), VectorMath.mag2d(computeClosestPoint(new Edge(a, b), point).subtract(point)));
        edges[1] = new EdgeWithDistance(new Edge(b, c), VectorMath.mag2d(computeClosestPoint(new Edge(b, c), point).subtract(point)));
        edges[2] = new EdgeWithDistance(new Edge(c, a), VectorMath.mag2d(computeClosestPoint(new Edge(c, a), point).subtract(point)));

        Arrays.sort(edges);
        return edges[0];
    }
	
	
	
	private static Vector computeClosestPoint(Edge edge, Vector point) {
        Vector ab = edge.b.subtract(edge.a);
        double t = point.subtract(edge.a).dot(ab) / ab.dot(ab);

        if (t < 0.0d) {
            t = 0.0d;
        } else if (t > 1.0d) {
            t = 1.0d;
        }

        return edge.a.add(ab.multiply(t));
    }
	
	
	
	private static boolean hasSameSign(double a, double b) {
        return Math.signum(a) == Math.signum(b);
    }
	
	
	
	@Override
    public String toString() {
        return "Triangle[" + a + ", " + b + ", " + c + "]";
    }
	
}
