package com.mndk.scjd2mc.core.triangulator.cdt;

import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.Triangle;
import com.mndk.scjd2mc.core.util.shape.TriangleList;

import java.util.*;

/**
 * Data structure representing a 2D constrained Delaunay triangulation.
 * <p>
 * @see <a href="https://github.com/artem-ogre/CDT">https://github.com/artem-ogre/CDT</a>
 */
public class ConstraintDelaunayTriangulator {

	
	
	/**
	 * Constant representing no valid neighbor for a triangle
	 */
	static final int NO_NEIGHBOR = Integer.MAX_VALUE;
	
	
	
	/**
	 * Constant representing no valid vertex for a triangle
	 */
	static final int NO_VERTEX = Integer.MAX_VALUE;



	private static final double ROOT_3 = Math.sqrt(3);
	
	
	
	private static final Random RANDOM = new Random();
	
	
	
	/**
	 * Vertices vector
	 */
	public List<Vertex> vertices;
	
	/**
	 * Triangulation's vertices
	 */
	public List<IndexTriangle> triangles;
	
	/**
	 * Triangulation's constraints
	 */
	public Set<IndexEdge> fixedEdges;
	
	private final List<Integer> dummyTriangles;
	
	private int nRandomSamples;

	private FindingClosestPoint closestPtMode;

	private int nTargetVerts;
	
	private SuperGeometryType superGeomType;
	
	
	
	
	/**
	 * Constructor
	 */
	public ConstraintDelaunayTriangulator(FindingClosestPoint closestPtMode, int nRandSamples) {
		this.nRandomSamples = nRandSamples;
		this.closestPtMode = closestPtMode;
		this.nTargetVerts = 0;
		this.superGeomType = SuperGeometryType.SUPER_TRIANGLE;
		
		this.vertices = new ArrayList<>();
		this.triangles = new ArrayList<>();
		this.dummyTriangles = new ArrayList<>();
		this.fixedEdges = new HashSet<>();
	}
	
	
	
	
	/**
	 * Constructor
	 */
	public ConstraintDelaunayTriangulator(FindingClosestPoint closestPtMode) {
		this(closestPtMode, 10);
	}
	
	
	
	
	/**
	 * Constructor
	 */
	public ConstraintDelaunayTriangulator() {
		this(FindingClosestPoint.CLOSEST_RANDOM, 10);
	}
	
	
	
	
	
	/**
	 * Insert vertices into triangulation
	 * @param vertices_ List of vertices
	 */
	public void insertVertices(List<Vector2DH> vertices_) {
		if(vertices.isEmpty()) {
			addSuperTriangle(Box2d.envelope(vertices_));
		}
		for(Vector2DH vertex : vertices_) {
			insertVertex(vertex);
		}
	}
	
	
	
	/**
	 * Insert constraints (fixed edges) into triangulation
	 * @param edges List of edges
	 */
	public void insertEdges(List<IndexEdge> edges) {
		for(IndexEdge edge : edges) {
			insertEdge(new IndexEdge(edge.v1 + nTargetVerts, edge.v2 + nTargetVerts));
		}
		eraseDummies();
	}
	
	
	
	/**
	 * Erase triangles adjacent to super triangle
	 * 
	 * Note: does nothing if custom geometry is used
	 */
	public void eraseSuperTriangle() {
		if(superGeomType != SuperGeometryType.SUPER_TRIANGLE) {
			return;
		}
		
		for(int iT = 0; iT < triangles.size(); ++iT) {
			Array3<Integer> vertices = triangles.get(iT).vertices;
			if(vertices.e1 < 3 || vertices.e2 < 3 || vertices.e3 < 3) {
				makeDummy(iT);
			}
		}
		
		eraseDummies();
		eraseSuperTriangleVertices();
	}
	
	
	
	/**
	 * Erase triangles outside of constrained boundary using growing
	 */
	public void eraseOuterTriangles() {
		Stack<Integer> seed = new Stack<>();
		seed.push(vertices.get(0).triangles.get(0));
		Set<Integer> toErase = growToBoundary(seed);
		eraseTrianglesAtIndices(toErase);
		eraseSuperTriangleVertices();
	}
	
	
	
	/**
	 * Erase triangles outside of constrained boundary and auto-detected holes
	 * 
	 * Note: detecting holes relies on layer peeling based on layer depth.
	 */
	public void eraseOuterTrianglesAndHoles() {
		List<Integer> triDepths = calculateTriangleDepths(vertices, triangles, fixedEdges);
		
		List<Integer> toErase = new ArrayList<>(triangles.size());
		for(int iT = 0; iT < triangles.size(); ++iT) {
			if(triDepths.get(iT) % 2 == 0) {
				toErase.add(iT);
			}
		}
		
		eraseTrianglesAtIndices(toErase);
		eraseSuperTriangleVertices();
	}
	
	
	
	/**
	 * Call this method after directly setting custom super-geometry via
	 * vertices and triangles members.
	 */
	public void initializedWithCustomSuperGeometry() {
		nTargetVerts = vertices.size();
		superGeomType = SuperGeometryType.CUSTOM;
	}
	
	
	
	/**
	 * Combines index triangles and points into Vector2DH arrays.
	 * @return List of triangles
	 */
	public TriangleList getTriangles() {
		TriangleList result = new TriangleList();
		for (IndexTriangle it : triangles) {
			result.add(new Triangle(
					vertices.get(it.vertices.e1).pos,
					vertices.get(it.vertices.e2).pos,
					vertices.get(it.vertices.e3).pos
			));
		}
		return result;
	}
	
	
	
	private void addSuperTriangle(Box2d box) {
		nTargetVerts = 3;
		superGeomType = SuperGeometryType.SUPER_TRIANGLE;
		
		Vector2DH center = new Vector2DH((box.min.x + box.max.x) / 2, (box.min.z + box.max.z) / 2);
		double w = box.max.x - box.min.x;
		double h = box.max.z - box.min.z;
		double r = Math.sqrt(w * w + h * h) / 2; // incircle radius
		r *= 1.1;
		double R = 2 * r;                        // excircle radius
		double shiftX = R * ROOT_3 / 2;          // R * cos(30 deg)
		Vector2DH posV1 = new Vector2DH(center.x - shiftX, center.z - r);
		Vector2DH posV2 = new Vector2DH(center.x + shiftX, center.z - r);
		Vector2DH posV3 = new Vector2DH(center.x, center.z + R);
		vertices.add(new Vertex(posV1, 0));
		vertices.add(new Vertex(posV2, 0));
		vertices.add(new Vertex(posV3, 0));
		IndexTriangle superTri = new IndexTriangle(
				new Array3<>(0, 1, 2), 
				new Array3<>(NO_NEIGHBOR, NO_NEIGHBOR, NO_NEIGHBOR)
		);
		addTriangle(superTri);
	}
	
	
	
	private void insertVertex(Vector2DH pos) {
		int iVert = vertices.size();
		int[] trisAt = walkingSearchTrianglesAt(pos);
		Stack<Integer> triStack = 
				trisAt[1] == NO_NEIGHBOR ? insertPointInTriangle(pos, trisAt[0])
						                 : insertPointOnEdge(pos, trisAt[0], trisAt[1]);
		
		while(!triStack.isEmpty()) {
			
			int iT = triStack.pop();
			
			IndexTriangle t = triangles.get(iT);
			
			int iTopo = CDTUtils.opposedTriangle(t, iVert);
			if(iTopo == NO_NEIGHBOR) {
				continue;
			}
			
			boolean flipNeeded = isFlipNeeded(pos, iT, iTopo, iVert);
			if(flipNeeded) {
				flipEdge(iT, iTopo);
				triStack.push(iT);
				triStack.push(iTopo);
			}
		}				
	}
	
	
	
	private void insertEdge(IndexEdge edge) {
		int iA = edge.v1, iB = edge.v2;
		if(iA == iB) { // edge connects a vertex to itself
			return;
		}
		Vertex a = vertices.get(iA);
		Vertex b = vertices.get(iB);
		if(CDTUtils.verticesShareEdge(a, b)) {
			fixedEdges.add(new IndexEdge(iA, iB));
			return;
		}
		int[] intrsctdTrnglArr = intersectedTriangle(iA, a.triangles, a.pos, b.pos);
		int iT = intrsctdTrnglArr[0];
		int iVleft = intrsctdTrnglArr[1], iVright = intrsctdTrnglArr[2];
		
		//if one of the triangle vertices is on the edge, move edge start
		if(iT == NO_NEIGHBOR) {
			fixedEdges.add(new IndexEdge(iA, iVleft));
			insertEdge(new IndexEdge(iVleft, iB));
			return;
		}
		List<Integer> intersected = new ArrayList<>(Collections.singletonList(iT));
		List<Integer> ptsLeft = new ArrayList<>(Collections.singletonList(iVleft));
		List<Integer> ptsRight = new ArrayList<>(Collections.singletonList(iVright));
		int iV = iA;
		IndexTriangle t = triangles.get(iT);
		Array3<Integer> tverts = t.vertices;
		while(!tverts.contains(iB)) {
			int iTopo = CDTUtils.opposedTriangle(t, iV);
			IndexTriangle tOpo = triangles.get(iTopo);
			int iVopo = CDTUtils.opposedVertex(tOpo, iT);
			Vertex vOpo = vertices.get(iVopo);
			
			intersected.add(iTopo);
			iT = iTopo;
			t = triangles.get(iT);
			tverts = t.vertices;
			
			PtLineLocation loc = CDTUtils.locatePointLine(vOpo.pos, a.pos, b.pos);
			
			if(loc == PtLineLocation.LEFT) {
				ptsLeft.add(iVopo);
				iV = iVleft;
				iVleft = iVopo;
			}
			else if(loc == PtLineLocation.RIGHT) {
				ptsRight.add(iVopo);
				iV = iVright;
				iVright = iVopo;
			}
			else {
				iB = iVopo;
			}
		}
		// Remove intersected triangles
		for(int it : intersected) {
			makeDummy(it);
		}
		// Triangulate pseudo-polygons on both sides
		int iTleft = triangulatePseudopolygon(iA, iB, ptsLeft);
		Collections.reverse(ptsRight);
		int iTright = triangulatePseudopolygon(iB, iA, ptsRight);
		changeNeighbor(iTleft, NO_NEIGHBOR, iTright);
		changeNeighbor(iTright, NO_NEIGHBOR, iTleft);
		// add fixed edge
		fixedEdges.add(new IndexEdge(iA, iB));
		if(iB != edge.v2) {
			insertEdge(new IndexEdge(iB, edge.v2));
		}
	}
	
	
	
	/**
	 * @return [IndexTriangle index, Vertex index, Vertex index]
	 */
	private int[] intersectedTriangle(int iA, List<Integer> candidates, Vector2DH a, Vector2DH b) {
		for(int iT : candidates) {
			IndexTriangle t = triangles.get(iT);
			int i = CDTUtils.vertexIndex(t, iA);
			int iP1 = t.vertices.get(CDTUtils.cw(i));
			int iP2 = t.vertices.get(CDTUtils.ccw(i));
			PtLineLocation locP1 = CDTUtils.locatePointLine(vertices.get(iP1).pos, a, b);
			PtLineLocation locP2 = CDTUtils.locatePointLine(vertices.get(iP2).pos, a, b);
			if(locP2 == PtLineLocation.RIGHT) {
				if(locP1 == PtLineLocation.ON_LINE) {
					return new int[] {NO_NEIGHBOR, iP1, iP2};
				}
				if(locP1 == PtLineLocation.LEFT) {
					return new int[] {iT, iP1, iP2};
				}
			}
		}
		throw new RuntimeException("Could not find vertex triangle intersected by edge. " +
		                           "Note: can be caused by duplicate points.");
	}
	
	
	
	/**
	 * Insert point into triangle: split into 3 triangles:
	 *  - create 2 new triangles
	 *  - re-use old triangle for the 3rd
	 *                      v3
	 *                    / | \
	 *                   /  |  \ <-- original triangle (t)
	 *                  /   |   \
	 *              n3 /    |    \ n2
	 *                /newT2|newT1\
	 *               /      v      \
	 *              /    __/ \__    \
	 *             /  __/       \__  \
	 *            / _/      t'     \_ \
	 *          v1 ___________________ v2
	 *                     n1
	 * @param iT IndexTriangle index
	 * @return Indices of three resulting triangles
	 */
	private Stack<Integer> insertPointInTriangle(Vector2DH pos, int iT) {
		int v = vertices.size();
		int iNewT1 = addTriangle();
		int iNewT2 = addTriangle();
		
		IndexTriangle t = triangles.get(iT);
		Array3<Integer> vv = t.vertices;
		Array3<Integer> nn = t.neighbors;
		int v1 = vv.e1, v2 = vv.e2, v3 = vv.e3;
		int n1 = nn.e1, n2 = nn.e2, n3 = nn.e3;
		// make two new triangles and convert current triangle to 3rd new triangle
		triangles.set(iNewT1, new IndexTriangle(new Array3<>(v2, v3, v), new Array3<>(n2, iNewT2, iT)));
		triangles.set(iNewT2, new IndexTriangle(new Array3<>(v3, v1, v), new Array3<>(n3, iT, iNewT1)));
		triangles.set(iT, t = new IndexTriangle(new Array3<>(v1, v2, v), new Array3<>(n1, iNewT1, iNewT2)));
		// make and add a new vertex
		vertices.add(new Vertex(pos, iT, iNewT1, iNewT2));
		// adjust lists of adjacent triangles for v1, v2, v3
		addAdjacentTriangle(v1, iNewT2);
		addAdjacentTriangle(v2, iNewT1);
		removeAdjacentTriangle(v3, iT);
		addAdjacentTriangle(v3, iNewT1);
		addAdjacentTriangle(v3, iNewT2);
		// change triangle neighbor's neighbors to new triangles
		changeNeighbor(n2, iT, iNewT1);
		changeNeighbor(n3, iT, iNewT2);
		// return newly added triangles
		Stack<Integer> newTriangles = new Stack<>();
		newTriangles.push(iT);
		newTriangles.push(iNewT1);
		newTriangles.push(iNewT2);
		return newTriangles;
	}
	
	
	
	/**
	 * Inserting a point on the edge between two triangles
	 *    T1 (top)        v1
	 *                   /|\
	 *              n1 /  |  \ n4
	 *               /    |    \
	 *             /  T1' | Tnew1\
	 *           v2-------v-------v4
	 *             \ Tnew2| T2'  /
	 *               \    |    /
	 *              n2 \  |  / n3
	 *                   \|/
	 *   T2 (bottom)      v3
	 *
	 * @param iT1 IndexTriangle index #1
	 * @param iT2 IndexTriangle index #2
	 * @return Indices of four resulting triangles
	 */
	private Stack<Integer> insertPointOnEdge(Vector2DH pos, int iT1, int iT2) {
		int v = vertices.size();
		int iTnew1 = addTriangle();
		int iTnew2 = addTriangle();
		
		IndexTriangle t1 = triangles.get(iT1);
		IndexTriangle t2 = triangles.get(iT2);
		int i = CDTUtils.opposedVertexIndex(t1, iT2);
		int v1 = t1.vertices.get(i);
		int v2 = t1.vertices.get(CDTUtils.ccw(i));
		int n1 = t1.neighbors.get(i);
		int n4 = t1.neighbors.get(CDTUtils.cw(i));
		i = CDTUtils.opposedVertexIndex(t2, iT1);
		int v3 = t2.vertices.get(i);
		int v4 = t2.vertices.get(CDTUtils.ccw(i));
		int n3 = t2.neighbors.get(i);
		int n2 = t2.neighbors.get(CDTUtils.cw(i));
		// add new triangles and change existing ones
		triangles.set(iT1, t1 = new IndexTriangle(new Array3<>(v1, v2, v), new Array3<>(n1, iTnew2, iTnew1)));
		triangles.set(iT2, t2 = new IndexTriangle(new Array3<>(v3, v4, v), new Array3<>(n3, iTnew1, iTnew2)));
		triangles.set(iTnew1, new IndexTriangle(new Array3<>(v1, v, v4), new Array3<>(iT1, iT2, n4)));
		triangles.set(iTnew2, new IndexTriangle(new Array3<>(v3, v, v2), new Array3<>(iT2, iT1, n2)));
		// make and add new vertex
		vertices.add(new Vertex(pos, iT1, iTnew2, iT2, iTnew1));
		// adjust neighboring triangles and vertices
		changeNeighbor(n4, iT1, iTnew1);
	    changeNeighbor(n2, iT2, iTnew2);
	    addAdjacentTriangle(v1, iTnew1);
	    addAdjacentTriangle(v3, iTnew2);
	    removeAdjacentTriangle(v2, iT2);
	    addAdjacentTriangle(v2, iTnew2);
	    removeAdjacentTriangle(v4, iT1);
	    addAdjacentTriangle(v4, iTnew1);
	    // return newly added triangles
	    Stack<Integer> newTriangles = new Stack<>();
	    newTriangles.push(iT1);
	    newTriangles.push(iTnew2);
	    newTriangles.push(iT2);
	    newTriangles.push(iTnew1);
	    return newTriangles;
	}
	
	
	
	/**
	 * @return [IndexTriangle index, IndexTriangle index]
	 */
	private int[] trianglesAt(Vector2DH pos) {
		int[] out = {NO_NEIGHBOR, NO_NEIGHBOR};
	    for(int i = 0; i < triangles.size(); ++i) {
	        IndexTriangle t = triangles.get(i);
	        Vector2DH v1 = vertices.get(t.vertices.e1).pos;
	        Vector2DH v2 = vertices.get(t.vertices.e2).pos;
	        Vector2DH v3 = vertices.get(t.vertices.e3).pos;
	        PtTriLocation loc = CDTUtils.locatePointTriangle(pos, v1, v2, v3);
	        if(loc == PtTriLocation.OUTSIDE) {
	            continue;
	        }
	        out[0] = i;
	        if(CDTUtils.onEdge(loc)) {
	            out[1] = t.neighbors.get(CDTUtils.edgeNeighbor(loc));
	        }
	        return out;
	    }
	    throw new RuntimeException("No triangle was found at position");
	}
	
	
	
	/**
	 * @return [IndexTriangle index, IndexTriangle index]
	 */
	private int[] walkingSearchTrianglesAt(Vector2DH pos) {
		int[] out = {NO_NEIGHBOR, NO_NEIGHBOR};
	    // Query RTree for a vertex close to pos, to start the search
	    int startVertex = nearestVertexRand(pos, nRandomSamples);
	    int iT = walkTriangles(startVertex, pos);
	    // Finished walk, locate point in current triangle
	    IndexTriangle t = triangles.get(iT);
	    Vector2DH v1 = vertices.get(t.vertices.e1).pos;
	    Vector2DH v2 = vertices.get(t.vertices.e2).pos;
	    Vector2DH v3 = vertices.get(t.vertices.e3).pos;
	    PtTriLocation loc = CDTUtils.locatePointTriangle(pos, v1, v2, v3);
	    if(loc == PtTriLocation.OUTSIDE) {
	        throw new RuntimeException("No triangle was found at position");
	    }
	    out[0] = iT;
	    if(CDTUtils.onEdge(loc)) {
	        out[1] = t.neighbors.get(CDTUtils.edgeNeighbor(loc));
	    }
	    return out;
	}
	
	
	
	/**
	 * @return IndexTriangle index
	 */
	private int walkTriangles(int startVertex, Vector2DH pos) {
		// begin walk in search of triangle at pos
	    int currTri = vertices.get(startVertex).triangles.get(0);
	    Set<Integer> visited = new HashSet<>();
	    boolean found = false;
	    while(!found) {
	        IndexTriangle t = triangles.get(currTri);
	        found = true;
	        // stochastic offset to randomize which edge we check first
	        int offset = Math.abs(RANDOM.nextInt()) % 3;
	        for(int i_ = 0; i_ < 3; ++i_) {
	            int i = (i_ + offset) % 3;
	            Vector2DH vStart = vertices.get(t.vertices.get(i)).pos;
	            Vector2DH vEnd = vertices.get(t.vertices.get(CDTUtils.ccw(i))).pos;
	            PtLineLocation edgeCheck = CDTUtils.locatePointLine(pos, vStart, vEnd);
	            if(edgeCheck == PtLineLocation.RIGHT &&
	               t.neighbors.get(i) != NO_NEIGHBOR &&
	               visited.add(t.neighbors.get(i))) {
	                found = false;
	                currTri = t.neighbors.get(i);
	                break;
	            }
	        }
	    }
	    return currTri;
	}
	
	
	
	/**
	 * @return Vertex index
	 */
	private int nearestVertexRand(Vector2DH pos, int nSamples) {
		// start search at a vertex close to pos based on random sampling
	    int out = Math.abs(RANDOM.nextInt()) % vertices.size();
	    double minDist = pos.distance2d(vertices.get(out).pos);
	    for(int iSample = 0; iSample < nSamples; ++iSample) {
	        int candidate = Math.abs(RANDOM.nextInt()) % vertices.size();
	        double candidateDist = pos.distance2d(vertices.get(candidate).pos);
	        if(candidateDist < minDist) {
	            minDist = candidateDist;
	            out = candidate;
	        }
	    }
	    return out;
	}
	
	
	
	// int nearestVertexRtree(Vector2DH pos);
	
	
	
	/**
	 * Handles super-triangle vertices.<br>
	 * Super-tri points are not infinitely far and influence the input points
	 * @param iT IndexTriangle index
	 * @param iTopo Opposed triangle index
	 * @param iVert Vertex index
	 */
	private boolean isFlipNeeded(Vector2DH pos, int iT, int iTopo, int iVert) {
		IndexTriangle tOpo = triangles.get(iTopo);
		int i = CDTUtils.opposedVertexIndex(tOpo, iT);
		int iVopo = tOpo.vertices.get(i);
		if(superGeomType == SuperGeometryType.SUPER_TRIANGLE) {
			if(iVert < 3 && iVopo < 3) { // opposed vertices belong to super-triangle
				return false;            // no flip is needed
			}
		}
		int iVcw = tOpo.vertices.get(CDTUtils.cw(i));
		int iVccw = tOpo.vertices.get(CDTUtils.ccw(i));
		Vector2DH v1 = vertices.get(iVcw).pos;
		Vector2DH v2 = vertices.get(iVopo).pos;
		Vector2DH v3 = vertices.get(iVccw).pos;
		if(superGeomType == SuperGeometryType.SUPER_TRIANGLE) {
			if(iVcw < 3) {
				return CDTUtils.locatePointLine(v1, v2, v3) == CDTUtils.locatePointLine(pos, v2, v3);
			}
			if(iVccw < 3) {
				return CDTUtils.locatePointLine(v3, v1, v2) == CDTUtils.locatePointLine(pos, v1, v2);
			}
		}
		return CDTUtils.inCircumCircle(pos, v1, v2, v3);
	}
	
	
	
	/**
	 * Flip edge between T and Topo:
	 *
	 *                v4         | - old edge
	 *               /|\         ~ - new edge
	 *              / | \
	 *          n3 /  T' \ n4
	 *            /   |   \
	 *           /    |    \
	 *     T -> v1~~~~~~~~~v3 <- Topo
	 *           \    |    /
	 *            \   |   /
	 *          n1 \Topo'/ n2
	 *              \ | /
	 *               \|/
	 *                v2
	 *
	 * @param iT IndexTriangle index
	 * @param iTopo Opposed triangle index
	 */
	private void flipEdge(int iT, int iTopo) {
		IndexTriangle t = triangles.get(iT);
	    IndexTriangle tOpo = triangles.get(iTopo);
	    Array3<Integer> triNs = t.neighbors;
	    Array3<Integer> triOpoNs = tOpo.neighbors;
	    Array3<Integer> triVs = t.vertices;
	    Array3<Integer> triOpoVs = tOpo.vertices;
	    // find vertices and neighbors
	    int i = CDTUtils.opposedVertexIndex(t, iTopo);
	    int v1 = triVs.get(i);
	    int v2 = triVs.get(CDTUtils.ccw(i));
	    int n1 = triNs.get(i);
	    int n3 = triNs.get(CDTUtils.cw(i));
	    i = CDTUtils.opposedVertexIndex(tOpo, iT);
	    int v3 = triOpoVs.get(i);
	    int v4 = triOpoVs.get(CDTUtils.ccw(i));
	    int n4 = triOpoNs.get(i);
	    int n2 = triOpoNs.get(CDTUtils.cw(i));
	    // change vertices and neighbors
	    triangles.set(iT, new IndexTriangle(new Array3<>(v4, v1, v3), new Array3<>(n3, iTopo, n4)));
	    triangles.set(iTopo, new IndexTriangle(new Array3<>(v2, v3, v1), new Array3<>(n2, iT, n1)));
	    // adjust neighboring triangles and vertices
	    changeNeighbor(n1, iT, iTopo);
	    changeNeighbor(n4, iTopo, iT);
	    addAdjacentTriangle(v1, iTopo);
	    addAdjacentTriangle(v3, iT);
	    removeAdjacentTriangle(v2, iT);
	    removeAdjacentTriangle(v4, iTopo);
	}
	
	
	
	/**
	 * @param iT IndexTriangle index
	 * @param oldNeighbor Old neighbor index
	 * @param newNeighbor New neighbor index
	 */
	private void changeNeighbor(int iT, int oldNeighbor, int newNeighbor) {
		if(iT == NO_NEIGHBOR) {
	        return;
		}
	    IndexTriangle t = triangles.get(iT);
	    t.neighbors.set(CDTUtils.neighborIndex(t, oldNeighbor), newNeighbor);
	}
	
	
	
	/**
	 * @param iT IndexTriangle index
	 * @param newNeighbor New neighbor index
	 */
	private void changeNeighbor(int iT, int iVedge1, int iVedge2, int newNeighbor) {
		IndexTriangle t = triangles.get(iT);
		t.neighbors.set(CDTUtils.opposedTriangleIndex(t, iVedge1, iVedge2), newNeighbor);
	}
	
	
	
	/**
	 * @param iVertex Vertex index
	 * @param iTriangle IndexTriangle index
	 */
	private void addAdjacentTriangle(int iVertex, int iTriangle) {
		vertices.get(iVertex).triangles.add(iTriangle);
	}
	
	
	
	/**
	 * @param iVertex Vertex index
	 * @param iTriangle IndexTriangle index
	 */
	private void removeAdjacentTriangle(int iVertex, int iTriangle) {
		List<Integer> tris = vertices.get(iVertex).triangles;
	    tris.remove((Integer) iTriangle);
	}
	
	
	
	private List<Integer>[] splitPseudopolygon(int vi, List<Integer> points) {
		@SuppressWarnings("unchecked")
		List<Integer>[] out = new List[] {new ArrayList<Integer>(), new ArrayList<Integer>()};
		int i = 0;
		for(; vi != points.get(i); ++i) {
			out[0].add(points.get(i));
		}
		for(++i; i < points.size(); ++i) {
			out[1].add(points.get(i));
		}
		return out;
	}
	
	
	
	/**
	 * @param ia Index of vertex a
	 * @param ib Index of vertex b
	 * @param points List of vertex index
	 * @return IndexTriangle index
	 */
	private int triangulatePseudopolygon(int ia, int ib, List<Integer> points) {
		if(points.isEmpty()) {
	        return pseudopolyOuterTriangle(ia, ib);
		}
	    int ic = findDelaunayPoint(ia, ib, points);
	    List<Integer>[] splitted = splitPseudopolygon(ic, points);
	    // triangulate splitted pseudo-polygons
	    int iT2 = triangulatePseudopolygon(ic, ib, splitted[1]);
	    int iT1 = triangulatePseudopolygon(ia, ic, splitted[0]);
	    // add new triangle
	    IndexTriangle t = new IndexTriangle(new Array3<>(ia, ib, ic), new Array3<>(NO_NEIGHBOR, iT2, iT1));
	    int iT = addTriangle(t);
	    // adjust neighboring triangles and vertices
	    if(iT1 != NO_NEIGHBOR) {
	        if(splitted[0].isEmpty()) {
	            changeNeighbor(iT1, ia, ic, iT);
	        }
	        else {
	            triangles.get(iT1).neighbors.set(0, iT);
	        }
	    }
	    if(iT2 != NO_NEIGHBOR) {
	        if(splitted[1].isEmpty()) {
	            changeNeighbor(iT2, ic, ib, iT);
	        }
	        else {
	            triangles.get(iT2).neighbors.set(0, iT);
	        }
	    }
	    addAdjacentTriangle(ia, iT);
	    addAdjacentTriangle(ib, iT);
	    addAdjacentTriangle(ic, iT);

	    return iT;
	}
	
	
	
	/**
	 * @param ia Index of vertex a
	 * @param ib Index of vertex b
	 * @param points List of vertex index
	 * @return Vertex index
	 */
	private int findDelaunayPoint(int ia, int ib, List<Integer> points) {
		assert !points.isEmpty();
	    Vector2DH a = vertices.get(ia).pos;
	    Vector2DH b = vertices.get(ib).pos;
	    int ic = points.get(0);
	    Vector2DH c = vertices.get(ic).pos;
	    for(int it : points) {
	        Vector2DH v = vertices.get(it).pos;
	        if(!CDTUtils.inCircumCircle(v, a, b, c)) {
	            continue;
	        }
	        ic = it;
	        c = vertices.get(ic).pos;
	    }
	    return ic;
	}
	
	
	
	/**
	 * @param ia Index of vertex a
	 * @param ib Index of vertex b
	 * @return IndexTriangle index
	 */
	private int pseudopolyOuterTriangle(int ia, int ib) {
		List<Integer> aTris = vertices.get(ia).triangles;
		List<Integer> bTris = vertices.get(ib).triangles;
		
	    for(int it : aTris) {
	        if(bTris.contains(it)) { 
	            return it;
	        }
	    }
	    return NO_NEIGHBOR;
	}
	
	
	
	/**
	 * @return IndexTriangle index
	 */
	private int addTriangle(IndexTriangle t) {
		if(dummyTriangles.isEmpty()) {
			triangles.add(t);
			return triangles.size() - 1;
		}
		int nxtDummy = dummyTriangles.remove(dummyTriangles.size() - 1);
		triangles.set(nxtDummy, t);
		return nxtDummy;
	}
	
	
	
	/**
	 * @return IndexTriangle index
	 */
	private int addTriangle() {
		if(dummyTriangles.isEmpty()) {
			IndexTriangle dummy = new IndexTriangle(
					new Array3<>(NO_VERTEX, NO_VERTEX, NO_VERTEX), 
					new Array3<>(NO_NEIGHBOR, NO_NEIGHBOR, NO_NEIGHBOR));
			triangles.add(dummy);
			return triangles.size() - 1;
		}
		return dummyTriangles.remove(dummyTriangles.size() - 1);
	}
	
	
	
	/**
	 * @param iT IndexTriangle index
	 */
	private void makeDummy(int iT) {
		IndexTriangle t = triangles.get(iT);
		
		removeAdjacentTriangle(t.vertices.e1, iT);
		removeAdjacentTriangle(t.vertices.e2, iT);
		removeAdjacentTriangle(t.vertices.e3, iT);
		
		changeNeighbor(t.neighbors.e1, iT, NO_NEIGHBOR);
		changeNeighbor(t.neighbors.e2, iT, NO_NEIGHBOR);
		changeNeighbor(t.neighbors.e3, iT, NO_NEIGHBOR);
		
		dummyTriangles.add(iT);
	}
	
	
	
	private void eraseDummies() {
		if(dummyTriangles.isEmpty()) {
			return;
		}
		
		Set<Integer> dummySet = new HashSet<>(dummyTriangles);
		Map<Integer, Integer> triIndMap = new HashMap<>();
		triIndMap.put(NO_NEIGHBOR, NO_NEIGHBOR);
		
		for(int iT = 0, iTnew = 0; iT < triangles.size(); ++iT) {
			if(dummySet.contains(iT)) {
				continue;
			}
			triIndMap.put(iT, iTnew);
			triangles.set(iTnew, triangles.get(iT));
			iTnew++;
		}
		triangles = triangles.subList(0, triangles.size() - dummySet.size());
		
		// remap adjacent triangle indices for vertices
		for(Vertex v : vertices) {
			List<Integer> vTris = v.triangles;
			for(int i = 0; i < vTris.size(); i++) {
				int iT = vTris.get(i);
				vTris.set(i, triIndMap.get(iT));
			}
		}
		
		// remap neighbor indices for triangles
		for(IndexTriangle t : triangles) {
			t.neighbors.e1 = triIndMap.get(t.neighbors.e1);
			t.neighbors.e2 = triIndMap.get(t.neighbors.e2);
			t.neighbors.e3 = triIndMap.get(t.neighbors.e3);
		}
		
		// clear dummy triangles
		dummyTriangles.clear();
		
	}
	
	
	
	/**
	 * Note: No effect if custom geometry is used
	 */
	private void eraseSuperTriangleVertices() {
		if(superGeomType != SuperGeometryType.SUPER_TRIANGLE) {
			return;
		}
		
		for(IndexTriangle t : triangles) {
			t.vertices.e1 -= 3; t.vertices.e2 -= 3; t.vertices.e3 -= 3;
		}
		
		Set<IndexEdge> updatedFixedEdges = new HashSet<>();
		for(IndexEdge e : fixedEdges) {
			updatedFixedEdges.add(new IndexEdge(e.v1 - 3, e.v2 - 3));
		}
		fixedEdges = updatedFixedEdges;
		
		vertices = vertices.subList(3, vertices.size());
	}
	
	
	
	private void eraseTrianglesAtIndices(Iterable<Integer> indices) {
		for(int i : indices) {
			makeDummy(i);
		}
		eraseDummies();
	}
	
	
	
	/**
	 * @param seeds List of triangle index
	 * @return IndexTriangle index set
	 */
	private Set<Integer> growToBoundary(Stack<Integer> seeds) {
		Set<Integer> traversed = new HashSet<>();
		while(!seeds.isEmpty()) {
			int iT = seeds.pop();
			traversed.add(iT);
			IndexTriangle t = triangles.get(iT);
			
			for(int i = 0; i < 3; ++i) {
				IndexEdge opEdge = new IndexEdge(t.vertices.get(CDTUtils.ccw(i)), t.vertices.get(CDTUtils.cw(i)));
				if(fixedEdges.contains(opEdge)) {
					continue;
				}
				int iN = t.neighbors.get(CDTUtils.opoNbr(i));
				if(iN != NO_NEIGHBOR && !traversed.contains(iN)) {
					seeds.push(iN);
				}
			}
		}
		return traversed;
	}
	
	
	
	/**
	 * Calculate depth of each triangle in constraint triangulation.
	 * <p>
	 * Perform depth peeling from super triangle to outermost boundary,
	 * then to next boundary and so on until all triangles are traversed.
	 * <p>
	 * For example depth is:<br>
	 *  - 0 for triangles outside outermost boundary<br>
	 *  - 1 for triangles inside boundary but outside hole<br>
	 *  - 2 for triangles in hole<br>
	 *  - 3 for triangles in island and so on...<br>
	 *  
	 * @param vertices Vertices of triangulation
	 * @param triangles Triangles of triangulation
	 * @param fixedEdges Constraint edges of triangulation
	 * @return List where (index i) -> depth of i-th triangle
	 */
	public static List<Integer> calculateTriangleDepths(
			List<Vertex> vertices,
			List<IndexTriangle> triangles,
			Set<IndexEdge> fixedEdges
	) {
		Integer[] triDepthArray = new Integer[triangles.size()];
		Arrays.fill(triDepthArray, Integer.MAX_VALUE);
		List<Integer> triDepths = new ArrayList<>(Arrays.asList(triDepthArray));
		Stack<Integer> seeds = new Stack<>();
		seeds.push(vertices.get(0).triangles.get(0));
		int layerDepth = 0;
		
		do {
			Set<Integer> newSeeds = peelLayer(seeds, triangles, fixedEdges, layerDepth++, triDepths);
			seeds = new Stack<>();
			for(int newSeed : newSeeds) {
				seeds.push(newSeed);
			}
		} while(!seeds.empty());
		
		return triDepths;
	}
	
	
	
	/**
	 * Depth-peel a layer in triangulation, used when calculating triangle depths.
	 * <p>
	 * It takes starting seed triangles, traverses neighboring triangles, and
	 * assigns given layer depth to the traversed triangles. Traversal is blocked by
	 * constraint edges. Triangles behind constraint edges are recorded as seeds of
	 * next layer and returned from the function.
	 * 
	 * @param seeds Indices of seed triangles
	 * @param triangles Triangles of triangulation
	 * @param fixedEdges Constraint edges of triangulation
	 * @param layerDepth Current layer's depth to mark triangles with
	 * @param triDepths Depths of triangles
	 * @return Triangles of the next layer that are adjacent to the peeled layer.<br>
	 *         To be used as seeds when peeling the next layer.
	 */
	public static Set<Integer> peelLayer(
			Stack<Integer> seeds,
			List<IndexTriangle> triangles,
			Set<IndexEdge> fixedEdges,
			int layerDepth,
			List<Integer> triDepths
	) {
		Set<Integer> behindBoundary = new HashSet<>();
		while(!seeds.empty()) {
			int iT = seeds.pop();
			triDepths.set(iT, layerDepth);
			behindBoundary.remove(iT);
			IndexTriangle t = triangles.get(iT);
			for(int i = 0; i < 3; ++i) {
				IndexEdge opEdge = new IndexEdge(t.vertices.get(CDTUtils.ccw(i)), t.vertices.get(CDTUtils.cw(i)));
				int iN = t.neighbors.get(CDTUtils.opoNbr(i));
				if(iN == NO_NEIGHBOR || triDepths.get(iN) <= layerDepth) {
					continue;
				}
				if(fixedEdges.contains(opEdge)) {
					behindBoundary.add(iN);
					continue;
				}
				seeds.push(iN);
			}
		}
		return behindBoundary;
	}
	
	
}
