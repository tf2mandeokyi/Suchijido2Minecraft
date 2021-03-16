package com.mndk.kmdi.core.util.shape;

import java.util.ArrayList;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

@SuppressWarnings("serial")
public class TriangleList extends ArrayList<Triangle> {

	public Triangle findContainingTriangle(Vector point) {
        for (Triangle triangle : this) {
            if (triangle.contains(point) != null) return triangle;
        }
        return null;
    }
	
	
	
	public double interpolateY(Vector2D point) {
		for(Triangle triangle : this) {
			if(triangle.contains_line(point)) {
				return triangle.interpolateY(point);
			}
		}
		return Double.NaN;
	}
}
