package com.mndk.scjdmc.terrain;

import com.mndk.scjdmc.cdtlib.IndexedEdge;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.column.ScjdElement;
import com.mndk.scjdmc.column.ScjdElevatedElement;
import com.mndk.scjdmc.util.FeatureGeometryUtils;
import com.mndk.scjdmc.util.math.Vector2DH;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;

import java.util.*;

public class Contours2VertexEdgesConverter {

    private final List<Vector2DH[]> lineStrings;
    private List<Vector2DH> vertices;
    private Set<IndexedEdge> indexedEdges;
    private boolean finalized;

    public Contours2VertexEdgesConverter() {
        this.lineStrings = new ArrayList<>();
        this.finalized = false;
    }

    public void addLineStrings(List<Vector2DH[]> lineStrings) {
        this.lineStrings.addAll(lineStrings);
    }

    public void addLineStringCollection(
            SimpleFeatureCollection contourCollection, LayerDataType layerDataType
    ) {
        if(!layerDataType.isElementClassInstanceOf(ScjdElevatedElement.class)) return;

        List<Vector2DH[]> result = new ArrayList<>(contourCollection.size());
        SimpleFeatureIterator contourIterator = contourCollection.features();

        while(contourIterator.hasNext()) {
            ScjdElement element = layerDataType.toElementObject(contourIterator.next());
            double height = ((ScjdElevatedElement) element).getElevation();
            Geometry geometry = (Geometry) element.geometryObject;
            result.addAll(FeatureGeometryUtils.geometryToVector2DH(geometry, height));
        }
        contourIterator.close();

        this.addLineStrings(result);
    }

    public List<Vector2DH> getVertices() {
        if(!finalized) throw new UnsupportedOperationException("converter not yet finalized");
        return this.vertices;
    }

    public Set<IndexedEdge> getIndexedEdges() {
        if(!finalized) throw new UnsupportedOperationException("converter not yet finalized");
        return this.indexedEdges;
    }

    public void convert() {

        Map<Vector2DH, Pair<List<Integer>, List<Integer>>> vertexMap = new HashMap<>();
        Pair<List<Integer>, List<Integer>> emptyPair = Pair.of(new ArrayList<>(), new ArrayList<>());

        int edgeIndex = 0;
        for(Vector2DH[] lineString : this.lineStrings) {
            if(lineString.length == 0) continue;
            if(lineString.length == 1) {
                vertexMap.putIfAbsent(lineString[0], emptyPair);
            }

            Pair<List<Integer>, List<Integer>> leftListPair, rightListPair = null;
            for(int i = 0; i < lineString.length - 1; i++) {
                Vector2DH leftVertex = lineString[i], rightVertex = lineString[i + 1];

                if(i != 0) leftListPair = rightListPair;
                else leftListPair = vertexMap.computeIfAbsent(leftVertex, v -> Pair.of(new ArrayList<>(), new ArrayList<>()));
                rightListPair = vertexMap.computeIfAbsent(rightVertex, v -> Pair.of(new ArrayList<>(), new ArrayList<>()));

                leftListPair.getLeft().add(edgeIndex);
                rightListPair.getRight().add(edgeIndex);

                edgeIndex++;
            }
        }

        int vertexIndex = 0;
        this.vertices = new ArrayList<>(vertexMap.size());
        Map<Integer, MutablePair<Integer, Integer>> edgeMap = new HashMap<>(edgeIndex);
        for(Map.Entry<Vector2DH, Pair<List<Integer>, List<Integer>>> entry : vertexMap.entrySet()) {
            Vector2DH vertex = entry.getKey();
            Pair<List<Integer>, List<Integer>> pair = entry.getValue();
            List<Integer> leftEdgeIndexes = pair.getLeft(), rightEdgeIndexes = pair.getRight();

            vertices.add(vertex);

            for(int leftEdgeIndex : leftEdgeIndexes) {
                edgeMap.computeIfAbsent(leftEdgeIndex, i -> MutablePair.of(-1, -1)).setLeft(vertexIndex);
            }
            for(int rightEdgeIndex : rightEdgeIndexes) {
                edgeMap.computeIfAbsent(rightEdgeIndex, i -> MutablePair.of(-1, -1)).setRight(vertexIndex);
            }
            vertexIndex++;
        }

        this.indexedEdges = new HashSet<>(vertexMap.size());
        for(MutablePair<Integer, Integer> edgePair : edgeMap.values()) {
            if(!Objects.equals(edgePair.left, edgePair.right))
                indexedEdges.add(new IndexedEdge(edgePair.left, edgePair.right));
        }

        this.finalized = true;
    }

}
