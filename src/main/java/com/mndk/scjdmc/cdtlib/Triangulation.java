package com.mndk.scjdmc.cdtlib;

import com.mndk.scjdmc.util.math.Vector2DH;

import java.util.List;
import java.util.Set;

public class Triangulation {

    @SuppressWarnings("unused")
    private long triangulationPointer;


    // ### Constructors ###


    /**
     * Default constructor.
     */
    public Triangulation() {
        this.construct();
    }
    private native void construct();

    /**
     * Constructor.
     * @param vertexInsertionOrder strategy used for ordering vertex insertions
     */
    public Triangulation(VertexInsertionOrder vertexInsertionOrder) {
        this.construct(vertexInsertionOrder);
    }
    private native void construct(VertexInsertionOrder vertexInsertionOrder);

    /**
     * Constructor.
     * @param vertexInsertionOrder strategy used for ordering vertex insertions
     * @param intersectingEdgesStrategy strategy for treating intersecting constraint edges
     * @param minDistToConstraintEdge distance within which point is considered to be lying on a constraint edge.
     *                                Used when adding constraints to the triangulation.
     */
    public Triangulation(
            VertexInsertionOrder vertexInsertionOrder,
            IntersectingConstraintEdges intersectingEdgesStrategy,
            double minDistToConstraintEdge
    ) {
        this.construct(vertexInsertionOrder, intersectingEdgesStrategy, minDistToConstraintEdge);
    }
    private native void construct(
            VertexInsertionOrder vertexInsertionOrder,
            IntersectingConstraintEdges intersectingEdgesStrategy,
            double minDistToConstraintEdge
    );


    // ### Getters ###


    /**
     * @return triangulation's vertices
     */
    public native List<Vector2DH> getVertices();

    /**
     * @return triangulation's triangles
     */
    public native List<IndexedTriangle> getTriangles();

    /**
     * @return triangulation's constraints (fixed edges)
     */
    public native Set<IndexedEdge> getFixedEdges();

    // vertTris
    // overlapCount
    // pieceToOriginals


    // ### Methods ###


    /**
     * Insert vertices into triangulation.
     * @param vertices vector of vertices to insert
     */
    public native void insertVertices(List<Vector2DH> vertices);

    /**
     * Insert constraint edges into triangulation.
     * @param edges constraint edges
     */
    public native void insertEdges(List<IndexedEdge> edges);

    /**
     * Ensure that triangulation conforms to constraints (fixed edges)
     * @param edges edges to conform to
     */
    public native void conformToEdges(List<IndexedEdge> edges);

    /**
     * Erase triangles adjacent to super triangle.
     */
    public native void eraseSuperTriangle();

    /**
     * Erase triangles outside of constrained boundary using growing.
     */
    public native void eraseOuterTriangles();

    /**
     * Erase triangles outside of constrained boundary and auto-detected holes.
     */
    public native void eraseOuterTrianglesAndHoles();

    /**
     * Call this method after directly setting custom super-geometry via vertices and triangles members.
     */
    public native void initializedWithCustomSuperGeometry();

    /**
     * Check if the triangulation was finalized with erase... method and super-triangle was removed.
     * @return true if triangulation is finalized, false otherwise
     */
    public native boolean isFinalized();

    /**
     * Calculate depth of each triangle in constraint triangulation.
     * <p>
     * Supports overlapping boundaries.
     * <p>
     * Perform depth peeling from super triangle to outermost boundary,
     * then to next boundary and so on until all triangles are traversed.
     * <p>
     * For example depth is:
     * <ul>
     *     <li>0 for triangles outside outermost boundary</li>
     *     <li>1 for triangles inside boundary but outside hole</li>
     *     <li>2 for triangles in hole</li>
     *     <li>3 for triangles in island and so on...</li>
     * </ul>
     * @return vector where element at index i stores depth of i-th triangle
     */
    public native short calculateTriangleDepths();

    /**
     * Flip an edge between two triangle.
     * @param iT first triangle
     * @param iTopo second triangle
     */
    public native void flipEdge(int iT, int iTopo);

    /**
     * Remove triangles with specified indices.
     * <p>
     * Adjust internal triangulation state accordingly.
     * @param removedTriangles indices of triangles to remove
     */
    public native void removeTriangles(Set<Integer> removedTriangles);


    static {
        System.loadLibrary("cdt_handler");
    }
}
