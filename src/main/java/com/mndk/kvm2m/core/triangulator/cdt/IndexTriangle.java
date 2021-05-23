package com.mndk.kvm2m.core.triangulator.cdt;

/**
 * Triangulation triangle (Counter-clockwise winding)
 * 
 * @author artem-ogre
 *
 */
public class IndexTriangle {

	public Array3<Integer> vertices; // IndexTriangle's vertices
	public Array3<Integer> neighbors; // IndexTriangle's three neighbors
	
	public IndexTriangle(Array3<Integer> vertices, Array3<Integer> neighbors) {
		this.vertices = vertices;
		this.neighbors = neighbors;
	}
	
	@Override
	public String toString() {
		return "IndexTriangle{v=" + vertices + ", n=" + neighbors + "}";
	}
	
}
