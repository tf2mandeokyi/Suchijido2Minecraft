package com.mndk.kvm2m.core.triangulator;

import com.mndk.kvm2m.core.triangulator.cdt.ConstraintDelaunayTriangulator;
import com.mndk.kvm2m.core.triangulator.cdt.IndexEdge;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapParserResult;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;

import java.util.*;
import java.util.stream.Collectors;

public class TerrainTriangulator {
	
	
	public static TriangleList generate(VMapParserResult result) {
		
		List<VMapContour> contourList = 
				result.getLayer(VMapElementType.등고선).stream().map(element -> (VMapContour) element).collect(Collectors.toList());
		List<Vector2DH> elevationPoints = 
				result.getLayer(VMapElementType.표고점).stream().map(element -> ((VMapElevationPoint) element).toVector()).collect(Collectors.toList());
		
		return generate(contourList, elevationPoints);
	}
	
	
	
	public static TriangleList generate(List<VMapContour> contours, List<Vector2DH> elevationPoints) {
		Vector2DH[][] vertexes = new Vector2DH[contours.size()][];

		for(int i = 0; i < contours.size(); i++) {
			VMapContour contour = contours.get(i);
			Vector2DH[] vertex = contour.getVertexList()[0];
			Vector2DH[] destination = vertexes[i] = new Vector2DH[vertex.length];

			for(int j = 0; j < vertex.length; j++) {
				destination[j] = vertex[j].withHeight(contour.elevation);
			}
		}

		ConstraintDelaunayTriangulator cdt = new ConstraintDelaunayTriangulator();

		Map.Entry<List<Vector2DH>, List<IndexEdge>> parsed = vertexesToPointsAndIntegers(
				vertexes, elevationPoints.toArray(new Vector2DH[0]));

		cdt.insertVertices(parsed.getKey());
		cdt.insertEdges(parsed.getValue());

		cdt.eraseSuperTriangle();

		return cdt.getTriangles();
		// return new ConstraintDelaunayTriangulator(vertexes, elevationPoints.toArray(new Vector2DH[0])).getTriangleList();
	}



	private static Map.Entry<List<Vector2DH>, List<IndexEdge>> vertexesToPointsAndIntegers(
			Vector2DH[][] vertexes, Vector2DH[] additionalPoints) {

		Map<Vector2DH, Integer> tempMap = new HashMap<>();
		int i = 0;

		List<IndexEdge> indexEdges = new ArrayList<>();
		for (Vector2DH[] vertex : vertexes) {

			Vector2DH point = vertex[0];
			Integer prevIndex = tempMap.get(point), index;

			if (prevIndex == null) {
				tempMap.put(point, i);
				prevIndex = i++;
			}

			for (int k = 1; k < vertex.length; k++) {
				point = vertex[k];
				index = tempMap.get(point);

				if (index == null) {
					tempMap.put(point, i);
					index = i++;
				}

				indexEdges.add(new IndexEdge(prevIndex, index));

				prevIndex = index;
			}
		}

		for (Vector2DH point : additionalPoints) {
			tempMap.put(point, i++);
		}

		Vector2DH[] pointArray = new Vector2DH[tempMap.size()];

		for(Map.Entry<Vector2DH, Integer> entry : tempMap.entrySet()) {
			pointArray[entry.getValue()] = entry.getKey();
		}

		return new AbstractMap.SimpleEntry<>(Arrays.asList(pointArray), indexEdges);

	}
	
}
