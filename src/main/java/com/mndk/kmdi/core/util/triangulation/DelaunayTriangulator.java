package com.mndk.kmdi.core.util.triangulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mndk.kmdi.core.util.shape.Triangle;
import com.sk89q.worldedit.Vector;

public class DelaunayTriangulator {
	
	private List<Vector> points;
	private TriangleList triangleList;
	
	public DelaunayTriangulator(List<Vector> points) {
        this.points = points;
        this.triangleList = new TriangleList();
    }
	
	public DelaunayTriangulator triangulate() throws IllegalArgumentException {
		
        this.triangleList = new TriangleList();

        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("Less than three points in point set.");
        }
        
        double maxOfAnyCoordinate = 0.0d;

        for (Vector vector : points) {
            maxOfAnyCoordinate = Math.max(Math.max(vector.getX(), vector.getZ()), maxOfAnyCoordinate);
        }

        maxOfAnyCoordinate *= 16.0d;

        Vector p1 = new Vector(0.0d, 0, 3.0d * maxOfAnyCoordinate);
        Vector p2 = new Vector(3.0d * maxOfAnyCoordinate, 0, 0.0d);
        Vector p3 = new Vector(-3.0d * maxOfAnyCoordinate, 0, -3.0d * maxOfAnyCoordinate);

        Triangle superTriangle = new Triangle(p1, p2, p3);

        triangleList.add(superTriangle);

        for (int i = 0; i < points.size(); i++) {
            Triangle triangle = triangleList.findContainingTriangle(points.get(i));

            if (triangle == null) {
            	
                Edge edge = triangleList.findNearestEdge(points.get(i));

                Triangle first = triangleList.findOneTriangleSharing(edge);
                Triangle second = triangleList.findNeighbour(first, edge);

                Vector firstNoneEdgeVertex = first.getNoneEdgeVertex(edge);
                Vector secondNoneEdgeVertex = second.getNoneEdgeVertex(edge);

                triangleList.remove(first);
                triangleList.remove(second);

                Triangle triangle1 = new Triangle(edge.a, firstNoneEdgeVertex, points.get(i));
                Triangle triangle2 = new Triangle(edge.b, firstNoneEdgeVertex, points.get(i));
                Triangle triangle3 = new Triangle(edge.a, secondNoneEdgeVertex, points.get(i));
                Triangle triangle4 = new Triangle(edge.b, secondNoneEdgeVertex, points.get(i));

                triangleList.add(triangle1);
                triangleList.add(triangle2);
                triangleList.add(triangle3);
                triangleList.add(triangle4);

                legalizeEdge(triangle1, new Edge(edge.a, firstNoneEdgeVertex), points.get(i));
                legalizeEdge(triangle2, new Edge(edge.b, firstNoneEdgeVertex), points.get(i));
                legalizeEdge(triangle3, new Edge(edge.a, secondNoneEdgeVertex), points.get(i));
                legalizeEdge(triangle4, new Edge(edge.b, secondNoneEdgeVertex), points.get(i));
                
            } 
            else {

                Vector a = triangle.a, b = triangle.b, c = triangle.c;

                triangleList.remove(triangle);

                Triangle first = new Triangle(a, b, points.get(i));
                Triangle second = new Triangle(b, c, points.get(i));
                Triangle third = new Triangle(c, a, points.get(i));

                triangleList.add(first);
                triangleList.add(second);
                triangleList.add(third);

                legalizeEdge(first, new Edge(a, b), points.get(i));
                legalizeEdge(second, new Edge(b, c), points.get(i));
                legalizeEdge(third, new Edge(c, a), points.get(i));
            }
        }

        triangleList.removeTrianglesUsing(superTriangle.a);
        triangleList.removeTrianglesUsing(superTriangle.b);
        triangleList.removeTrianglesUsing(superTriangle.c);
        
        return this;
    }
	
	
	
	private void legalizeEdge(Triangle triangle, Edge edge, Vector newVertex) {
        Triangle neighbourTriangle = triangleList.findNeighbour(triangle, edge);

        if (neighbourTriangle != null) {
            if (neighbourTriangle.isPointInCircumcircle(newVertex)) {
                triangleList.remove(triangle);
                triangleList.remove(neighbourTriangle);

                Vector noneEdgeVertex = neighbourTriangle.getNoneEdgeVertex(edge);

                Triangle firstTriangle = new Triangle(noneEdgeVertex, edge.a, newVertex);
                Triangle secondTriangle = new Triangle(noneEdgeVertex, edge.b, newVertex);

                triangleList.add(firstTriangle);
                triangleList.add(secondTriangle);

                legalizeEdge(firstTriangle, new Edge(noneEdgeVertex, edge.a), newVertex);
                legalizeEdge(secondTriangle, new Edge(noneEdgeVertex, edge.b), newVertex);
            }
        }
    }
	
	
	
	public void shuffle() {
        Collections.shuffle(points);
    }


	
    public void shuffle(int[] permutation) {
        List<Vector> temp = new ArrayList<Vector>();
        for (int i = 0; i < permutation.length; i++) {
            temp.add(points.get(permutation[i]));
        }
        points = temp;
    }


    
    public List<Vector> getPointSet() {
        return points;
    }


    
    public TriangleList getTriangles() {
        return triangleList;
    }
    
    
    
    public static void main(String[] args) {
		TriangleList list = 
				new DelaunayTriangulator(Arrays.asList(new Vector(0, 0, 0), new Vector(1, 3, 0), new Vector(1, 3, 1), new Vector(0, 4, 1)))
					.triangulate()
					.getTriangles();
		System.out.println(list);
		
	}
}
