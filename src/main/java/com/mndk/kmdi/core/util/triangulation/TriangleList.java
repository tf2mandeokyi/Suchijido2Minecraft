package com.mndk.kmdi.core.util.triangulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mndk.kmdi.core.util.shape.Triangle;
import com.sk89q.worldedit.Vector;

@SuppressWarnings("serial")
public class TriangleList extends ArrayList<Triangle> {

	public Triangle findContainingTriangle(Vector point) {
        for (Triangle triangle : this) {
            if (triangle.contains(point)) return triangle;
        }
        return null;
    }
	
	
	
	public Triangle findNeighbour(Triangle triangle, Edge edge) {
        for (Triangle triangleFromSoup : this) {
            if (triangleFromSoup.isNeighbour(edge) && triangleFromSoup != triangle) {
            	return triangleFromSoup;
            }
        }
        return null;
    }
	
	
	
	public Triangle findOneTriangleSharing(Edge edge) {
        for (Triangle triangle : this) {
            if (triangle.isNeighbour(edge)) return triangle;
        }
        return null;
    }
	
	
	
	public Edge findNearestEdge(Vector point) {
        List<EdgeWithDistance> edgeList = new ArrayList<EdgeWithDistance>();

        for (Triangle triangle : this) {
            edgeList.add(triangle.findNearestEdge(point));
        }

        EdgeWithDistance[] edgeDistancePacks = new EdgeWithDistance[edgeList.size()];
        edgeList.toArray(edgeDistancePacks);

        Arrays.sort(edgeDistancePacks);
        return edgeDistancePacks[0].edge;
    }
	
	
	
	public void removeTrianglesUsing(Vector vertex) {
        List<Triangle> trianglesToBeRemoved = new ArrayList<Triangle>();

        for (Triangle triangle : this) {
            if (triangle.hasVertex(vertex)) {
                trianglesToBeRemoved.add(triangle);
            }
        }

        removeAll(trianglesToBeRemoved);
    }
	
}
