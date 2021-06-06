package com.mndk.kvm2m.core.triangulator;

import com.mndk.kvm2m.core.triangulator.cdt.ConstraintDelaunayTriangulator;
import com.mndk.kvm2m.core.triangulator.cdt.IndexEdge;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapParserResult;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;

import java.util.*;
import java.util.stream.Collectors;

public class TerrainTriangulator {
	
	
	public static TriangleList generate(VMapParserResult result) {

		List<VMapContour> contourList = new ArrayList<>();
		List<Vector2DH> elevationPoints = new ArrayList<>();
		VMapLayer tempLayer;

		if((tempLayer = result.getLayer(VMapElementType.등고선)) != null) {
			contourList = tempLayer.stream().map(element -> (VMapContour) element).collect(Collectors.toList());
		}

		if((tempLayer = result.getLayer(VMapElementType.표고점)) != null) {
			for(VMapElement pElem : tempLayer) {
				assert pElem instanceof VMapElevationPoint;
				VMapElevationPoint point = (VMapElevationPoint) pElem;

				if( checkLayerContainsPoint(point, result.getLayer(VMapElementType.육교)) ||
					checkLayerContainsPoint(point, result.getLayer(VMapElementType.교량)) ||
					checkLayerContainsPoint(point, result.getLayer(VMapElementType.입체교차부))) {

					continue;
				}

				elevationPoints.add(point.getPosition());
			}
		}


		
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



	private static boolean checkLayerContainsPoint(VMapPoint point, VMapLayer layer) {
		if(layer == null) return true;

		for(VMapElement elem : layer) {
			if(elem instanceof VMapPolygon) {
				if(((VMapPolygon) elem).containsPoint(point.getPosition())) {
					return true;
				}
			}
		}
		return false;
	}
}
