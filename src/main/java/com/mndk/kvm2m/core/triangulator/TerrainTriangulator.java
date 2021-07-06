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
import com.mndk.kvm2m.core.vmap.elem.line.VMapPolyline;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;

import java.util.*;
import java.util.stream.Collectors;

public class TerrainTriangulator {



	public static TriangleList generateTerrain(VMapParserResult result) {

		Map.Entry<List<VMapContour>, List<Vector2DH>> extraction = extractContourAndElevationPointList(result);

		TriangleList first = generateTerrain(extraction.getKey(), extraction.getValue(), Collections.emptyList());

		TriangleList second = generateWithLayerPolylines(result, extraction, first, VMapElementType.도로중심선);

		return generateWithLayerPolylines(result, extraction, second, VMapElementType.도로경계);

	}



	private static TriangleList generateWithLayerPolylines(
			VMapParserResult result,
			Map.Entry<List<VMapContour>, List<Vector2DH>> extraction,
			TriangleList previousResult,
			VMapElementType layerType) {

		List<VMapPolyline> roadCenterLines = new ArrayList<>();

		VMapLayer tempLayer;
		if((tempLayer = result.getLayer(layerType)) != null) {
			roadCenterLines = tempLayer.stream().map(element -> (VMapPolyline) element).collect(Collectors.toList());
		}

		List<Vector2DH[]> roadCenterLineEdges = new ArrayList<>();

		for(VMapPolyline line : roadCenterLines) {
			Vector2DH[][] vertexList = line.getVertexList();
			for(Vector2DH[] vertex : vertexList) {
				for(int i = 0; i < vertex.length - 1; ++i) {
					Vector2DH p0 = vertex[i], p1 = vertex[i + 1];

					p0 = p0.withHeight(previousResult.interpolateHeight(p0.x, p0.z));
					p1 = p1.withHeight(previousResult.interpolateHeight(p1.x, p1.z));

					if(Double.isNaN(p0.height) || Double.isNaN(p1.height)) continue;

					roadCenterLineEdges.add(new Vector2DH[] { p0, p1 });
				}
			}
		}

		return generateTerrain(extraction.getKey(), extraction.getValue(), roadCenterLineEdges);

	}
	
	
	
	private static TriangleList generateTerrain(
			List<VMapContour> contours, List<Vector2DH> elevationPoints, List<Vector2DH[]> additionalEdges) {

		List<Vector2DH[]> vertexes = new ArrayList<>();

		for (VMapContour contour : contours) {
			Vector2DH[] vertex = contour.getVertexList()[0];
			Vector2DH[] destination = new Vector2DH[vertex.length];

			for (int j = 0; j < vertex.length; j++) {
				destination[j] = vertex[j].withHeight(contour.elevation);
			}

			vertexes.add(destination);
		}

		vertexes.addAll(additionalEdges);

		Map.Entry<List<Vector2DH>, List<IndexEdge>> parsed = vertexesToPointsAndIntegers(
				vertexes, elevationPoints.toArray(new Vector2DH[0]));

		ConstraintDelaunayTriangulator cdt = new ConstraintDelaunayTriangulator();

		cdt.insertVertices(parsed.getKey());
		cdt.insertEdges(parsed.getValue());

		cdt.eraseSuperTriangle();

		return cdt.getTriangles();
	}



	static Map.Entry<List<VMapContour>, List<Vector2DH>> extractContourAndElevationPointList(VMapParserResult result) {

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

		return new AbstractMap.SimpleEntry<>(contourList, elevationPoints);

	}



	private static Map.Entry<List<Vector2DH>, List<IndexEdge>> vertexesToPointsAndIntegers(
			List<Vector2DH[]> vertexes, Vector2DH[] additionalPoints) {

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
