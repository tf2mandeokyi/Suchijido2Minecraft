package com.mndk.kvm2m.core.util.triangulator;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import com.mndk.kvm2m.core.util.math.Vector2DH;

public class ConstraintDelaunayTriangulator extends FastDelaunayTriangulator {

	
	private final int[][] vertices;
	
	
	public ConstraintDelaunayTriangulator(Vector2DH[][] vertexes) {
		this(vertexesToPointsAndIntegers(vertexes));
	}
	
	
	public ConstraintDelaunayTriangulator(Vector2DH[][] vertexes, Vector2DH[] additionalPoints) {
		this(vertexesToPointsAndIntegers(vertexes, additionalPoints));
	}

	
	private ConstraintDelaunayTriangulator(Map.Entry<Vector2DH[], int[][]> entry) {
		this(entry.getKey(), entry.getValue());
	}

	
	protected ConstraintDelaunayTriangulator(Vector2DH[] coords, int[][] vertexes) {
		
		super(coords);
		
		this.vertices = vertexes;
		
		// TODO this.updateVertexes();
		
	}



	private void updateVertexes() {
		// TODO Auto-generated method stub
		
		// 1. Loop over each constrained edge.
		//   Let each constrained edge be defined by the vertices Vi and Vj. For each of these edges, do step 2 ~ 4.
		
		// 2. Find intersecting edges.
		//   If the constrained edge Vi-Vj is already present in the triangulation, then go to step 1.
		//   Else, search the triangulation and store all of the edges that cross Vi-Vj.
		
		// 3. Remove intersecting edges.
		//   While some edges still cross the constrained edge Vi-Vj, do steps 3.1 and 3.2.
		//   
		//     3.1. Remove an edge from the list of edges that intersect Vi-Vj.
		//          Let this edge be defined by the vertices Vk and Vl.
		//
		//     3.2. If the two triangles that share the edge Vk-Vl do not form a quadrilateral which is
		//          strictly convex, then place Vk-Vl back on the list of intersecting edges and go to 3.1.
		//          Else, swap the diagonal of this strictly convex quadrilateral so that two new triangles
		//          are substituted for two old triangles.
		//          Let the new diagonal be defined by the vertices Vm and Vn. If Vm-Vn still intersects
		//          the constrained edge Vi-Vj, then place it on the list of intersecting edges.
		//          If Vm-Vn does not intersect Vi-Vj, then place Vm-Vn on a list of newly created edges.
		
		// 4. Restore Delaunay triangulation
		//   Repeat steps 4.1 ~ 4.3 until no further swaps take place.
		//
		//     4.1. Loop over each edge in the list of newly created edges.
		//
		//     4.2. Let the newly created edge be defined by the vertices Vk and Vl. If the edge Vk-Vl is
		//          equal to the constrained edge Vi-Vj, then skip to step 4.1.
		//
		//     4.3. If the two triangles that share the edge Vk-Vl do not satisfy the Delaunay criterion,
		//          so that a vertex of one of the triangles is inside the circumcircle of the other triangle,
		//          then these triangles form a quadrilateral with the diagonal drawn in the wrong direction.
		//          In this case, the edge Vk-Vl is swapped with the other diagonal (say) Vm-Vn, thus substituting
		//          two new triangles for two new triangles, and Vk-Vl is replaced by Vm-Vn in the list of newly
		//          created edges.
		
		// 5. Remove superfluous triangles.
		//   Remove all triangles that contain a supertriangle vertex or lie outside the domain boundary. 
		
		
		// Step 1.
		for(int[] vertex : vertices) {
		
			for(int i = 0; i < vertex.length - 1; i++) {
			
				int vi = vertex[i], vj = vertex[i + 1];
				
				// Step 2.
				int triangleIndex = getTriangleEdgeIndex(vi, vj);
				if(triangleIndex == -1) continue;
				
				// Step 3.1: Impossible, because the triangulation data is stored as a list of triangles
				// Instead, uh
				
			}
		}
	}
	
	
	
	private int getTriangleEdgeIndex(int p1, int p2) {
		
		int j;
		for(int i = 0; i < this.trianglesLen; i++) {
			j = i / 3 + ((i + 1) % 3);
			if((p1 == i && p2 == j) || (p1 == j && p2 == i)) {
				return i;
			}
		}
		return -1;
		
	}
	
	
	
	private boolean swapTriangle() {
		// TODO
		return false;
	}
	
	
	
	private static Map.Entry<Vector2DH[], int[][]> vertexesToPointsAndIntegers(Vector2DH[][] vertexes) {
		return vertexesToPointsAndIntegers(vertexes, new Vector2DH[0]);
	}
	
	
	
	private static Map.Entry<Vector2DH[], int[][]> vertexesToPointsAndIntegers(Vector2DH[][] vertexes, Vector2DH[] additionalPoints) {
		
		Map<Vector2DH, Integer> tempMap = new HashMap<>();
		int i = 0;
		
		int[][] integerVertexes = new int[vertexes.length][];
		for(int j = 0; j < vertexes.length; j++) {
			Vector2DH[] vertex = vertexes[j];
			integerVertexes[j] = new int[vertex.length];
			
			for(int k = 0; k < vertex.length; k++) {
				Vector2DH point = vertex[k];
				Integer tempInt = tempMap.get(point);
				
				if(tempInt != null) {
					integerVertexes[j][k] = tempInt;
				}
				else {
					tempMap.put(point, i);
					integerVertexes[j][k] = i++;
				}
			}
		}
		
		for(int j = 0; j < additionalPoints.length; j++) {
			Vector2DH point = additionalPoints[j];
			tempMap.put(point, i++);
		}
		
		Vector2DH[] pointList = new Vector2DH[tempMap.size()];
		
		for(Map.Entry<Vector2DH, Integer> entry : tempMap.entrySet()) {
			pointList[entry.getValue()] = entry.getKey();
		}
		
		return new AbstractMap.SimpleEntry<>(pointList, integerVertexes);
		
	}
	
}
