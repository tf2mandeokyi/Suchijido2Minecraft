package com.mndk.kvm2m.core.triangulator.cdt;

import com.mndk.kvm2m.core.util.math.Vector2DH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Triangulation vertex.
 * @author artem-ogre
 */
public class Vertex {

	/**
	 * Vertex position
	 */
	public Vector2DH pos;
	
	/**
	 * Adjacent triangles
	 */
	public List<Integer> triangles;
	
	
	
	public Vertex(final Vector2DH pos, final List<Integer> triangles) {
		this.pos = pos; this.triangles = triangles;
	}
	
	
	
	/**
	 * Create vertex
	 * @param pos
	 * @param iTriangle
	 * @return
	 */
	public Vertex(final Vector2DH pos, final int iTriangle) {
		this(pos, new ArrayList<>(Arrays.asList(iTriangle)));
	}
	
	/**
	 * Create vertex in a triangle.
	 * @param pos
	 * @param iT1
	 * @param iT2
	 * @param iT3
	 * @return
	 */
	public Vertex(final Vector2DH pos, final int iT1, final int iT2, final int iT3) {
		this(pos, new ArrayList<>(Arrays.asList(iT1, iT2, iT3)));
	}

	/**
	 * Create vertex on an edge.
	 * @param pos
	 * @param iT1
	 * @param iT2
	 * @param iT3
	 * @param iT4
	 * @return
	 */
	public Vertex(final Vector2DH pos, final int iT1, final int iT2, final int iT3, final int iT4) {
		this(pos, new ArrayList<>(Arrays.asList(iT1, iT2, iT3, iT4)));
	}
	
	@Override
	public String toString() {
		return "Vertex{p=" + pos + ", t=" + triangles + "}";
	}
}
