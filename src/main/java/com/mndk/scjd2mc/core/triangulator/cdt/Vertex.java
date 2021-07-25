package com.mndk.scjd2mc.core.triangulator.cdt;

import com.mndk.scjd2mc.core.util.math.Vector2DH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	 */
	public Vertex(final Vector2DH pos, final int iTriangle) {
		this(pos, new ArrayList<>(Collections.singletonList(iTriangle)));
	}
	
	/**
	 * Create vertex in a triangle.
	 */
	public Vertex(final Vector2DH pos, final int iT1, final int iT2, final int iT3) {
		this(pos, new ArrayList<>(Arrays.asList(iT1, iT2, iT3)));
	}

	/**
	 * Create vertex on an edge.
	 */
	public Vertex(final Vector2DH pos, final int iT1, final int iT2, final int iT3, final int iT4) {
		this(pos, new ArrayList<>(Arrays.asList(iT1, iT2, iT3, iT4)));
	}
	
	@Override
	public String toString() {
		return "Vertex{p=" + pos + ", t=" + triangles + "}";
	}
}
